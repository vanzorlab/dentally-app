package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.DailyMoodEntity
import com.example.data.local.HabitEntity
import com.example.data.local.OhisEntity
import com.example.data.repository.GamificationStatus
import com.example.data.repository.UserProfileData
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class DentallyFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndLoadUserData_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser("alice_${UUID.randomUUID()}@gmail.com")
        val repo = DentallyFirestoreRepository(firestore, auth)

        val profile = UserProfileData(
            userId = uid,
            userName = "Alice Gigi",
            userEmail = auth.currentUser?.email ?: "alice@gmail.com",
            isLoggedIn = true,
            isGoogleConnected = true
        )
        val status = GamificationStatus(
            toothHealthPercent = 95,
            toothStage = "Gigi Putih Berkilau",
            toothMood = "Super Kinclong",
            plaqueLevel = "Bebas Plak",
            streakDays = 7,
            totalBrushingSessions = 14,
            levelName = "Level 5",
            completedGoodHabits = 4,
            totalGoodHabits = 4
        )

        val profileSaved = withTimeout(TIMEOUT_MS) { repo.saveUserProfile(profile, status) }
        assertTrue(profileSaved)

        val habit = HabitEntity(
            id = 101L,
            title = "Sikat Gigi Pagi",
            subtitle = "2 Menit",
            isCompleted = true,
            dateString = "2026-11-15"
        )
        assertTrue(withTimeout(TIMEOUT_MS) { repo.saveHabit(habit) })

        val mood = DailyMoodEntity(
            dateString = "2026-11-15",
            moodScore = 5,
            moodTitle = "Luar Biasa",
            note = "Gigi segar seharian"
        )
        assertTrue(withTimeout(TIMEOUT_MS) { repo.saveMood(mood) })

        val ohis = OhisEntity(
            id = 202L,
            formattedDate = "15 Nov 2026",
            debrisIndex = 0.3,
            calculusIndex = 0.2,
            totalOhis = 0.5,
            category = "Baik",
            feedbackText = "Pertahankan!"
        )
        assertTrue(withTimeout(TIMEOUT_MS) { repo.saveOhis(ohis) })

        val loadedProfile = withTimeout(TIMEOUT_MS) { repo.loadUserProfile() }
        assertNotNull(loadedProfile)
        assertEquals("Alice Gigi", loadedProfile?.userName)

        val loadedHabits = withTimeout(TIMEOUT_MS) { repo.fetchAllHabits() }
        assertTrue(loadedHabits.any { it.title == "Sikat Gigi Pagi" && it.isCompleted })

        val loadedMoods = withTimeout(TIMEOUT_MS) { repo.fetchAllMoods() }
        assertTrue(loadedMoods.any { it.dateString == "2026-11-15" && it.moodScore == 5 })

        val loadedOhis = withTimeout(TIMEOUT_MS) { repo.fetchAllOhis() }
        assertTrue(loadedOhis.any { it.id == 202L && it.category == "Baik" })
    }

    @Test
    fun crossUserAccess_regularUserCannotReadOtherUserOrListUsers() = runBlocking {
        val aliceUid = signInTestUser("alice_iso_${UUID.randomUUID()}@gmail.com")
        val aliceRepo = DentallyFirestoreRepository(firestore, auth)
        val aliceProfile = UserProfileData(
            userId = aliceUid,
            userName = "Alice Private",
            userEmail = auth.currentUser?.email ?: "alice@gmail.com",
            isLoggedIn = true
        )
        assertTrue(withTimeout(TIMEOUT_MS) { aliceRepo.saveUserProfile(aliceProfile) })

        // Sign in as Bob (regular user)
        signInTestUser("bob_iso_${UUID.randomUUID()}@gmail.com")
        val bobRepo = DentallyFirestoreRepository(firestore, auth)

        // Bob cannot list all users
        val listResult = withTimeout(TIMEOUT_MS) { bobRepo.fetchAllUsersForDeveloper() }
        assertTrue(listResult.isFailure)
        val listEx = listResult.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, listEx?.code)

        // Bob cannot read Alice's user document directly
        var gotPermissionDenied = false
        try {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).get().await()
            }
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            if (fsEx?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                gotPermissionDenied = true
            }
        }
        assertTrue("Bob must be denied reading Alice's document", gotPermissionDenied)
    }

    @Test
    fun unauthenticatedAccess_deniedByRules() = runBlocking {
        auth.signOut()
        val repo = DentallyFirestoreRepository(firestore, auth)
        assertFalse(repo.isUserSignedIn())

        var denied = false
        try {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("users").get().await()
            }
        } catch (e: Exception) {
            if (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                denied = true
            }
        }
        assertTrue("Unauthenticated read of users must fail with PERMISSION_DENIED", denied)
    }

    private companion object {
        const val TIMEOUT_MS = 5000L
    }
}
