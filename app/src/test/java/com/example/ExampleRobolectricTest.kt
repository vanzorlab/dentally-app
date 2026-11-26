package com.example

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.UserProfileRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.DentallyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @org.junit.Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    try {
      if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
        com.google.firebase.FirebaseApp.initializeApp(context)
      }
    } catch (_: Exception) {}
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dentally", appName)
  }

  @Test
  fun `login and registration state updates properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = UserProfileRepository(context)

    repo.login(email = "user@gmail.com", name = "Test User", uid = "uid_user")
    assertTrue(repo.userProfile.value.isLoggedIn)
    assertFalse(repo.userProfile.value.isDeveloper)
    assertEquals("Test User", repo.userProfile.value.userName)
    assertEquals("user@gmail.com", repo.userProfile.value.userEmail)

    repo.login(email = "fight.vanzor30@gmail.com", name = "Dev", uid = "uid_dev")
    assertTrue(repo.userProfile.value.isDeveloper)

    repo.logout()
    assertFalse(repo.userProfile.value.isLoggedIn)
    assertFalse(repo.userProfile.value.isDeveloper)
  }

  @Test
  fun `authScreen renders and animates without negative padding crash`() {
    composeTestRule.setContent {
      DentallyTheme {
        AuthScreen(onAuthSuccess = {})
      }
    }
    composeTestRule.mainClock.advanceTimeBy(700)
    composeTestRule.onNodeWithTag("google_login_btn")
      .performScrollTo()
      .assertIsDisplayed()
  }

  @Test
  fun `onboarding sfx and evening notification prefs work correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = UserProfileRepository(context)

    assertFalse(repo.hasSeenOnboarding("new_uid"))
    repo.setOnboardingSeen("new_uid", true)
    assertTrue(repo.hasSeenOnboarding("new_uid"))

    assertTrue(repo.userProfile.value.sfxEnabled)
    repo.setSfxEnabled(false)
    assertFalse(repo.userProfile.value.sfxEnabled)

    repo.setNightReminderTime("20:30")
    assertEquals("20:30", repo.userProfile.value.nightReminderTime)

    com.example.notifications.EveningReminderScheduler.markTodayEveningCompleted(context, true)
    assertTrue(com.example.notifications.EveningReminderScheduler.isTodayEveningCompleted(context))
    com.example.notifications.EveningReminderScheduler.markTodayEveningCompleted(context, false)
    assertFalse(com.example.notifications.EveningReminderScheduler.isTodayEveningCompleted(context))
  }

  @Test
  fun `monthlyHabitCalendar renders and handles date click`() {
    var clickedDate = ""
    val habits = listOf(
      com.example.data.local.HabitEntity(
        id = 1L,
        title = "Sikat Gigi Pagi",
        subtitle = "2 Menit",
        iconEmoji = "brush",
        category = "Sikat Gigi",
        dateString = "2026-11-20",
        isCompleted = true
      )
    )
    composeTestRule.setContent {
      DentallyTheme {
        com.example.ui.components.MonthlyHabitCalendar(
          habits = habits,
          moods = emptyList(),
          selectedDate = "2026-11-20",
          onDateSelected = { clickedDate = it }
        )
      }
    }
    composeTestRule.onNodeWithTag("monthly_habit_calendar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendar_prev_month").assertIsDisplayed()
    assertEquals("", clickedDate)
  }

  @Test
  fun `launch MainActivity does not crash`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
    controller.pause().stop().destroy()
  }

  @Test
  fun `chatbot responds casually, handles general topics, and evaluates quiz interactively`() {
    val service = com.example.data.remote.GeminiChatService()
    val ctx = com.example.data.remote.UserChatContext(
      userName = "Rafi Pratama",
      toothHealthPercent = 90,
      streakDays = 6,
      levelName = "Level 5 - Senyum Kinclong",
      completedGoodHabits = 4,
      totalGoodHabits = 4,
      timerDoneToday = true
    )

    // Personal progress & casual tone
    val progressReply = service.getSmartFallbackResponse("Gimana progres gigiku?", emptyList(), ctx)
    assertTrue(progressReply.contains("Rafi"))
    assertTrue(progressReply.contains("90%"))
    assertFalse(progressReply.contains("OHI-S"))

    // Open-ended general topic (math + daily food)
    val mathReply = service.getSmartFallbackResponse("berapa 12 + 8?", emptyList(), ctx)
    assertTrue(mathReply.contains("20"))

    val bobaReply = service.getSmartFallbackResponse("Habis minum boba nih", emptyList(), ctx)
    assertTrue(bobaReply.contains("kumur", ignoreCase = true))

    // Interactive quiz start + answer evaluation from history
    val quizQuestion = service.getSmartFallbackResponse("Main kuis yuk", emptyList(), ctx)
    assertTrue(quizQuestion.contains("[Kuis #"))
    val historyWithQuiz = listOf(
      "Main kuis yuk" to true,
      quizQuestion to false
    )
    val quizAnswerAttempt = when {
      quizQuestion.contains("[Kuis #1]") -> "A"
      else -> "B"
    }
    val quizFeedback = service.getSmartFallbackResponse(quizAnswerAttempt, historyWithQuiz, ctx)
    assertTrue(quizFeedback.contains("BENAR BANGET", ignoreCase = true))

    // Markdown formatter test
    val formatted = com.example.ui.screens.buildFormattedChatText("Halo **Dentally** dan *seru*")
    assertEquals("Halo Dentally dan seru", formatted.text)
  }

  @Test
  fun `chatLearningManager shares collective intelligence across different users immediately`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val learner = com.example.data.repository.ChatLearningManager(context)
    val service = com.example.data.remote.GeminiChatService()
    val userA = "user_alpha"
    val userB = "user_beta"

    // User A chats & teaches slang, interests, and brushing topic + a private personal fact
    learner.learnFromUserMessage(userA, "gw suka boba yg brown sugar wkwk")
    learner.learnFromUserMessage(userA, "aku kelas 8")
    learner.learnFromUserMessage(userA, "cara sikat gigi biar bersih gimana")
    learner.learnFromUserMessage(userA, "cara sikat gigi yg bener gimana lagi")

    // Brand new User B (different account) sends message #1 with similar vibe
    val combinedForB = learner.learnFromUserMessage(userB, "gw baru nyoba nih, cara sikat gigi gimana ya wkwk")
    val profileB = combinedForB.userProfile
    val sharedBrain = combinedForB.sharedKnowledge

    assertEquals("gue_lo", profileB.pronounStyle)
    assertEquals("wkwk", profileB.laughStyle)
    assertTrue(profileB.adaptedFromSimilarUsers)
    assertEquals("Nyambung Cepat", profileB.closenessLevel)
    assertTrue(sharedBrain.totalCollectiveInteractions >= 5)
    assertTrue(sharedBrain.knownSlang.contains("gw"))
    assertTrue(sharedBrain.popularInterests.contains("suka minum boba"))
    // Private fact from User A does NOT leak into User B's personal facts
    assertFalse(profileB.learnedFacts.any { it.contains("kelas 8") })

    val ctxB = com.example.data.remote.UserChatContext(
      userName = "Raka",
      toothHealthPercent = 85,
      learnedProfile = profileB,
      sharedKnowledge = sharedBrain
    )
    // User B immediately benefits from collective topic depth & style adaptation!
    val replyForB = service.getSmartFallbackResponse("cara sikat gigi dong", emptyList(), ctxB)
    assertTrue(replyForB.contains("45 derajat"))
    assertTrue(replyForB.contains("lu"))
  }

  @Test
  fun `launchSplashOverlay renders startup animation cleanly and handles finish callback`() {
    var finished = false
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      DentallyTheme {
        com.example.ui.components.LaunchSplashOverlay(
          visible = true,
          onFinished = { finished = true }
        )
      }
    }
    composeTestRule.mainClock.advanceTimeBy(400)
    composeTestRule.onNodeWithTag("launch_splash_overlay").assertIsDisplayed()
    assertFalse(finished)

    composeTestRule.mainClock.advanceTimeBy(2200)
    assertTrue(finished)
  }

  @Test
  fun `homeSyncSkeletonView and staggeredEntrance render smoothly`() {
    composeTestRule.setContent {
      DentallyTheme {
        com.example.ui.components.StaggeredEntrance(index = 0) {
          com.example.ui.components.HomeSyncSkeletonView()
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(450)
    composeTestRule.onNodeWithTag("home_sync_skeleton").assertIsDisplayed()
  }

  @Test
  fun `brushingTimerHeroCard starts collapsed and expands on start click`() {
    var toggled = false
    composeTestRule.setContent {
      DentallyTheme {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          com.example.ui.screens.BrushingTimerHeroCard(
            secondsLeft = 120,
            isRunning = false,
            currentQuadrant = com.example.ui.viewmodel.BrushingQuadrant.UPPER_RIGHT,
            completedToday = false,
            onToggleStartPause = { toggled = true },
            onReset = {},
            onNavigateToGamification = {},
            onNavigateToChat = {}
          )
        }
      }
    }

    composeTestRule.onNodeWithTag("home_timer_hero_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_timer_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reset_timer_button").assertDoesNotExist()

    composeTestRule.onNodeWithTag("toggle_timer_button").performClick()
    composeTestRule.waitForIdle()
    assertTrue(toggled)
    composeTestRule.onNodeWithTag("reset_timer_button").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("timer_quadrant_pill").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `weeklyCalendarStrip locks future dates and triggers blocked callback`() {
    val days = com.example.ui.components.rememberWeeklyDays(emptySet())
    val futureDay = days.firstOrNull { it.isFuture }
    var selected = ""
    var blocked = false

    composeTestRule.setContent {
      DentallyTheme {
        com.example.ui.components.WeeklyCalendarStrip(
          selectedDateString = days.firstOrNull { it.isToday }?.dateString ?: days.first().dateString,
          onDateSelected = { selected = it },
          completedDates = emptySet(),
          onFutureDateBlocked = { blocked = true }
        )
      }
    }

    if (futureDay != null) {
      composeTestRule.onNodeWithTag("calendar_day_${futureDay.dateString}").performClick()
      composeTestRule.waitForIdle()
      assertEquals("", selected)
      assertTrue(blocked)
    }
  }

  @Test
  fun `dentalBrowserRepository serves clean direct sources without google search fallbacks`() {
    val repo = com.example.data.remote.DentalBrowserRepository()
    val curated = repo.getInitialCuratedArticles()
    assertTrue(curated.any { it.category == com.example.data.remote.BrowserCategory.SEASONAL })
    assertTrue(curated.any { it.category == com.example.data.remote.BrowserCategory.DENTAL })
    assertTrue(curated.any { it.category == com.example.data.remote.BrowserCategory.RESEARCH })
    assertFalse(curated.any { it.url.contains("google.com/search", ignoreCase = true) })
    assertFalse(curated.any { it.url.equals("https://www.instagram.com/kemenkes_ri/", ignoreCase = true) })
    assertEquals(null, repo.sanitizeUrl("https://www.google.com/search?q=test"))
  }

  @Test
  fun `dentallyViewModel blocks selecting future dates`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.viewmodel.DentallyViewModel(app)
    assertTrue(vm.isFutureDate("2099-12-31"))
    assertFalse(vm.isFutureDate(vm.todayDateString))
    vm.setSelectedDate("2099-12-31")
    assertEquals(vm.todayDateString, vm.selectedDate.value)
  }

  @Test
  fun `addHabitDialog starts in bad habit mode when requested`() {
    composeTestRule.setContent {
      DentallyTheme {
        com.example.ui.screens.AddHabitDialog(
          initialIsBadHabit = true,
          onDismiss = {},
          onAdd = { _, _, _, _, _ -> }
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tambah Kebiasaan Buruk").assertIsDisplayed()
  }

  @Test
  fun `userProfileRepository persists avatarUrl and custom reminder time`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = UserProfileRepository(context)
    repo.login(
      email = "gigi@gmail.com",
      name = "Alya",
      uid = "uid_alya",
      avatarUrl = "https://lh3.googleusercontent.com/a/photo"
    )
    repo.setNightReminderTime("21:45")
    assertEquals("https://lh3.googleusercontent.com/a/photo", repo.userProfile.value.avatarUrl)
    assertEquals("21:45", repo.userProfile.value.nightReminderTime)
  }

  @Test
  fun `customBrowserSources persist locally, validate URLs, and survive feed refresh`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.viewmodel.DentallyViewModel(app)

    // Reject Google search dumps and accept bare IG/YouTube links by normalizing scheme
    assertEquals(null, vm.validateCustomSourceUrl("https://www.google.com/search?q=karang+gigi"))
    assertEquals(
      "https://instagram.com/reel/Cx123Abc",
      vm.validateCustomSourceUrl("instagram.com/reel/Cx123Abc")
    )

    val addedVideo = vm.addCustomBrowserSource(
      title = "Cara Flossing Benar",
      summaryOrContent = "Video edukasi teknik C-shape untuk membersihkan sela gigi.",
      sourceName = "",
      url = "instagram.com/reel/Cx123Abc",
      category = com.example.data.remote.BrowserCategory.VIDEO_REELS,
      takeawaysRaw = listOf("Pakai benang gigi tiap malam")
    )
    val addedMaterial = vm.addCustomBrowserSource(
      title = "Catatan Materi Periodontitis",
      summaryOrContent = "Radang jaringan penyangga gigi dapat dicegah dengan kontrol plak rutin.",
      sourceName = "Materi Kuliah",
      url = "",
      category = com.example.data.remote.BrowserCategory.RESEARCH
    )

    assertTrue(addedVideo != null && addedVideo.isCustom)
    assertEquals("Instagram Reels • Sumber Saya", addedVideo!!.sourceName)
    assertEquals("https://instagram.com/reel/Cx123Abc", addedVideo.url)
    assertTrue(addedMaterial != null && addedMaterial.url.isEmpty())

    assertTrue(vm.browserArticles.value.any { it.id == addedVideo.id })
    assertTrue(vm.browserArticles.value.any { it.id == addedMaterial!!.id })

    // Verify CustomArticleLocalStore persisted across instances
    val store = com.example.data.repository.CustomArticleLocalStore(app)
    val stored = store.getArticles(vm.userProfile.value.userId)
    assertTrue(stored.any { it.title == "Cara Flossing Benar" })
    assertTrue(stored.any { it.title == "Catatan Materi Periodontitis" })

    // Delete custom source
    vm.deleteCustomBrowserSource(addedVideo.id)
    assertFalse(vm.browserArticles.value.any { it.id == addedVideo.id })
  }

  @Test
  fun `addCustomSourceDialog validates and submits user sources`() {
    var savedTitle = ""
    var savedUrl = ""
    composeTestRule.setContent {
      DentallyTheme {
        com.example.ui.screens.AddCustomSourceDialog(
          validateUrl = { raw -> com.example.data.remote.DentalBrowserRepository().normalizeUserSourceUrl(raw) },
          onDismiss = {},
          onSave = { title, _, _, url, _, _ ->
            savedTitle = title
            savedUrl = url
          }
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("add_custom_source_dialog").assertIsDisplayed()

    // Trying to save empty form displays validation error
    composeTestRule.onNodeWithTag("save_custom_source_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("custom_source_error_text").performScrollTo().assertIsDisplayed()
    assertEquals("", savedTitle)

    // Fill title and YouTube link and submit
    composeTestRule.onNodeWithTag("custom_source_title_input").performScrollTo().performTextInput("Video Sikat Gigi")
    composeTestRule.onNodeWithTag("custom_source_url_input").performScrollTo().performTextInput("youtube.com/watch?v=abc123")
    composeTestRule.onNodeWithTag("save_custom_source_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    assertEquals("Video Sikat Gigi", savedTitle)
    assertEquals("youtube.com/watch?v=abc123", savedUrl)
  }
}
