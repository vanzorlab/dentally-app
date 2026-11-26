const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const serverTimestamp = () => new Date(Date.now() - 1000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const DEV_UID = "dev_uid_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot list or read users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and read own profile, habits, moods, and ohis", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      email: "alice@gmail.com",
      streakDays: 3,
      toothHealthPercent: 90,
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("habits").doc("habit_1").set({
      userId: ALICE_UID,
      title: "Sikat Gigi Pagi",
      date: "2026-11-15",
      isCompleted: true,
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("moods").doc("2026-11-15").set({
      userId: ALICE_UID,
      date: "2026-11-15",
      moodScore: 5,
      moodLabel: "Luar Biasa",
      activities: ["sikat_pagi"],
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("ohis").doc("ohis_1").set({
      userId: ALICE_UID,
      date: "15 Nov 2026",
      debrisScore: 0.5,
      calculusScore: 0.3,
      totalScore: 0.8,
      category: "Baik",
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("habits").get());
});

test("Authenticated user: cannot list all users or read/write another user's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob",
      email: "bob@gmail.com",
      updatedAt: serverTimestamp(),
    });
    await db.collection("users").doc(BOB_UID).collection("habits").doc("b_habit").set({
      userId: BOB_UID,
      title: "Sikat Malam",
      date: "2026-11-15",
      isCompleted: true,
      updatedAt: serverTimestamp(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@gmail.com",
    email_verified: true,
  }).firestore();

  await assertFails(aliceDb.collection("users").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("habits").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Hacked",
      email: "alice@gmail.com",
      updatedAt: serverTimestamp(),
    })
  );
});

test("Developer (verified fight.vanzor30@gmail.com): can list all users and inspect their subcollections", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      email: "alice@gmail.com",
      updatedAt: serverTimestamp(),
    });
    await db.collection("users").doc(ALICE_UID).collection("ohis").doc("ohis_1").set({
      userId: ALICE_UID,
      date: "15 Nov 2026",
      debrisScore: 0.5,
      calculusScore: 0.2,
      totalScore: 0.7,
      category: "Baik",
      updatedAt: serverTimestamp(),
    });
    await db.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob",
      email: "bob@gmail.com",
      updatedAt: serverTimestamp(),
    });
  });

  const devDb = testEnv.authenticatedContext(DEV_UID, {
    email: "fight.vanzor30@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(devDb.collection("users").get());
  await assertSucceeds(devDb.collection("users").doc(ALICE_UID).get());
  await assertSucceeds(devDb.collection("users").doc(ALICE_UID).collection("ohis").get());
  await assertSucceeds(devDb.collection("users").doc(BOB_UID).get());

  // Developer still cannot overwrite another user's private document
  await assertFails(
    devDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Modified",
      email: "alice@gmail.com",
      updatedAt: serverTimestamp(),
    })
  );
});

test("Spoofed developer (email_verified=false): denied access to other users", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob",
      email: "bob@gmail.com",
      updatedAt: serverTimestamp(),
    });
  });

  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: "fight.vanzor30@gmail.com",
    email_verified: false,
  }).firestore();

  await assertFails(spoofDb.collection("users").get());
  await assertFails(spoofDb.collection("users").doc(BOB_UID).get());
});

test("Shared AI Knowledge (/ai_knowledge/global_brain): authenticated users share collective brain safely, unauthenticated denied", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@gmail.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@gmail.com",
    email_verified: true,
  }).firestore();

  await assertFails(unauthDb.collection("ai_knowledge").doc("global_brain").get());

  // Alice contributes collective learning
  await assertSucceeds(
    aliceDb.collection("ai_knowledge").doc("global_brain").set({
      lastContributorUid: ALICE_UID,
      totalCollectiveInteractions: 5,
      gueLoUsersCount: 2,
      akuKamuUsersCount: 3,
      knownSlang: ["santuy", "gaskeun"],
      popularInterests: ["suka minum boba"],
      updatedAt: serverTimestamp(),
    })
  );

  // Bob can read and build upon collective knowledge immediately
  await assertSucceeds(bobDb.collection("ai_knowledge").doc("global_brain").get());
  await assertSucceeds(
    bobDb.collection("ai_knowledge").doc("global_brain").set({
      lastContributorUid: BOB_UID,
      totalCollectiveInteractions: 6,
      knownSlang: ["santuy", "gaskeun", "mabar"],
      updatedAt: serverTimestamp(),
    })
  );

  // Bob cannot spoof Alice's UID or delete shared brain
  await assertFails(
    bobDb.collection("ai_knowledge").doc("global_brain").set({
      lastContributorUid: ALICE_UID,
      totalCollectiveInteractions: 7,
      updatedAt: serverTimestamp(),
    })
  );
  await assertFails(bobDb.collection("ai_knowledge").doc("global_brain").delete());
});

test("Custom Articles (/users/{uid}/custom_articles): owner can create, list, delete; cross-user denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@gmail.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, {
    email: "bob@gmail.com",
    email_verified: true,
  }).firestore();

  const ref = aliceDb.collection("users").doc(ALICE_UID).collection("custom_articles").doc("custom_12345");
  await assertSucceeds(
    ref.set({
      userId: ALICE_UID,
      title: "Video Cara Flossing yang Benar",
      summary: "Tutorial singkat membersihkan sela gigi",
      contentDetail: "Gunakan benang gigi dengan gerakan huruf C di sela gigi sebelum tidur.",
      sourceName: "YouTube • Sumber Saya",
      publishedAt: "26 Nov 2026",
      readTime: "Video",
      category: "VIDEO_REELS",
      url: "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      takeaways: ["Lakukan rutin tiap malam"],
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("custom_articles").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("custom_articles").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("custom_articles").doc("custom_12345").delete()
  );
  await assertSucceeds(ref.delete());
});
