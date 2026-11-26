package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.audio.SoundEffectManager
import com.example.notifications.EveningReminderScheduler
import com.example.ui.components.ConfettiBurstOverlay
import com.example.ui.components.DentallyLogoIcon
import com.example.ui.components.EveningCheckInGateDialog
import com.example.ui.components.LaunchSplashOverlay
import com.example.ui.components.OnboardingDemoDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DentalBrowserScreen
import com.example.ui.screens.DentallyChatScreen
import com.example.ui.screens.DeveloperDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ToothGamificationScreen
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTheme
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalMedium
import com.example.ui.theme.NaturalPrimary
import com.example.ui.viewmodel.DentallyViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Beranda", Icons.Filled.Home, Icons.Outlined.Home)
    object Browser : Screen("browser", "Berita", Icons.Filled.Public, Icons.Outlined.Public)
    object Gamification : Screen("gamification", "Gigi Ku", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Chat : Screen("chat", "Dentally", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Profile : Screen("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
    object Developer : Screen("developer", "Monitor", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {
    private var openEveningCheckInSignal by mutableIntStateOf(0)
    private var startBrushingTimerSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (_: Exception) {}
        EveningReminderScheduler.ensureChannel(this)
        handleReminderExtras(intent)
        enableEdgeToEdge()
        setContent {
            DentallyTheme {
                DentallyApp(
                    openEveningCheckInSignal = openEveningCheckInSignal,
                    startBrushingTimerSignal = startBrushingTimerSignal
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleReminderExtras(intent)
    }

    private fun handleReminderExtras(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra(EveningReminderScheduler.EXTRA_START_BRUSHING_TIMER, false)) {
            startBrushingTimerSignal++
        } else if (intent.getBooleanExtra(EveningReminderScheduler.EXTRA_OPEN_EVENING_CHECKIN, false)) {
            openEveningCheckInSignal++
        }
    }
}

@Composable
fun DentallyApp(
    openEveningCheckInSignal: Int = 0,
    startBrushingTimerSignal: Int = 0
) {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var currentUser by remember {
        mutableStateOf(
            try {
                Firebase.auth.currentUser
            } catch (_: Exception) {
                null
            }
        )
    }

    DisposableEffect(Unit) {
        val auth = try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth?.addAuthStateListener(listener)
        onDispose {
            auth?.removeAuthStateListener(listener)
        }
    }

    // Launch intro animation + silent sign-in coordination
    var showLaunchSplash by remember { mutableStateOf(true) }
    var isBootstrappingSession by remember { mutableStateOf(currentUser == null) }

    // Silent auto-sign-in attempt on startup using GetGoogleIdOption (separate request)
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            isBootstrappingSession = true
            attemptAutoSignIn(context, credentialManager)
            isBootstrappingSession = false
        } else {
            isBootstrappingSession = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentUser?.uid,
            transitionSpec = {
                (fadeIn(tween(380, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(380, easing = FastOutSlowInEasing)))
                    .togetherWith(
                        fadeOut(tween(240, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 1.03f, animationSpec = tween(240, easing = FastOutSlowInEasing))
                    )
            },
            label = "firebase_auth_gate"
        ) { uid ->
            if (uid == null || currentUser == null) {
                AuthScreen(
                    onAuthSuccess = {
                        currentUser = try {
                            Firebase.auth.currentUser
                        } catch (_: Exception) {
                            null
                        }
                    }
                )
            } else {
                val activeUser = currentUser!!
                key(uid) {
                    val dentallyViewModel: DentallyViewModel = viewModel(key = uid)
                    LaunchedEffect(uid) {
                        dentallyViewModel.onAuthenticatedUserReady(activeUser)
                    }
                    LaunchedEffect(openEveningCheckInSignal) {
                        if (openEveningCheckInSignal > 0) {
                            dentallyViewModel.triggerEveningCheckInGate(forceOpen = true)
                        }
                    }
                    DentallyMainScaffold(
                        viewModel = dentallyViewModel,
                        startBrushingTimerSignal = startBrushingTimerSignal
                    )
                }
            }
        }

        LaunchSplashOverlay(
            visible = showLaunchSplash,
            isBusyLoading = isBootstrappingSession,
            onFinished = { showLaunchSplash = false }
        )
    }
}

private suspend fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager
) {
    val auth = try {
        Firebase.auth
    } catch (_: Exception) {
        return
    }
    if (auth.currentUser != null) return

    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            auth.signInWithCredential(authCredential).await()
        }
    } catch (_: Exception) {
        // Silent sign-in failed or no previously authorized account; interactive AuthScreen remains shown
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DentallyMainScaffold(
    viewModel: DentallyViewModel,
    startBrushingTimerSignal: Int = 0
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val showOnboardingDemo by viewModel.showOnboardingDemo.collectAsStateWithLifecycle()
    val showEveningCheckInGate by viewModel.showEveningCheckInGate.collectAsStateWithLifecycle()
    val todayHabits by viewModel.todayHabits.collectAsStateWithLifecycle()
    val celebrationTrigger by viewModel.celebrationTrigger.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && userProfile.eveningNotificationEnabled) {
            val parts = userProfile.nightReminderTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            EveningReminderScheduler.scheduleDailyEveningReminder(context, hour, minute)
        }
    }

    LaunchedEffect(userProfile.eveningNotificationEnabled) {
        if (userProfile.eveningNotificationEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val screens = listOf(
        Screen.Home,
        Screen.Browser,
        Screen.Gamification,
        Screen.Chat,
        Screen.Profile
    )

    fun navigateToTab(route: String) {
        if (currentRoute != route) {
            SoundEffectManager.playPop()
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(startBrushingTimerSignal) {
        if (startBrushingTimerSignal > 0) {
            viewModel.dismissEveningGateTemporarilyForTimer()
            navigateToTab(Screen.Home.route)
            viewModel.startTimer()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = NaturalBackground,
            bottomBar = {
                if (currentRoute != Screen.Developer.route) {
                    Column {
                        HorizontalDivider(thickness = 1.dp, color = DentallyBorder)
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 0.dp
                        ) {
                        screens.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { navigateToTab(screen.route) },
                                icon = {
                                    when (screen) {
                                        Screen.Chat -> {
                                            DentallyLogoIcon(
                                                sizeDp = 26.dp,
                                                isSelected = isSelected
                                            )
                                        }
                                        Screen.Gamification -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) NaturalPrimary else DentallyTintBg
                                                    )
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isSelected) Color(0xFFFECDD3) else DentallyBorder,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                    contentDescription = screen.title,
                                                    tint = if (isSelected) Color.White else NaturalPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Screen.Profile -> {
                                            if (userProfile.avatarUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = userProfile.avatarUrl,
                                                    contentDescription = screen.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .border(
                                                            width = if (isSelected) 1.5.dp else 1.dp,
                                                            color = if (isSelected) Color.White else DentallyBorder,
                                                            shape = CircleShape
                                                        )
                                                        .testTag("nav_profile_avatar")
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                    contentDescription = screen.title,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected || screen == Screen.Gamification) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = NaturalPrimary,
                                    indicatorColor = if (screen == Screen.Gamification) Color.Transparent else NaturalPrimary,
                                    unselectedIconColor = NaturalMedium,
                                    unselectedTextColor = if (screen == Screen.Gamification) NaturalPrimary else NaturalMedium
                                ),
                                modifier = Modifier.testTag("nav_${screen.route}")
                            )
                        }
                    }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                enterTransition = {
                    fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                        slideInVertically(tween(220, easing = FastOutSlowInEasing)) { it / 28 }
                },
                exitTransition = {
                    fadeOut(tween(160, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                        slideInVertically(tween(220, easing = FastOutSlowInEasing)) { it / 28 }
                },
                popExitTransition = {
                    fadeOut(tween(160, easing = FastOutSlowInEasing))
                }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = { navigateToTab(Screen.Profile.route) },
                        onNavigateToGamification = { navigateToTab(Screen.Gamification.route) },
                        onNavigateToChat = { navigateToTab(Screen.Chat.route) },
                        onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) }
                    )
                }
                composable(Screen.Browser.route) {
                    DentalBrowserScreen(viewModel = viewModel)
                }
                composable(Screen.Gamification.route) {
                    ToothGamificationScreen(
                        viewModel = viewModel,
                        onNavigateToHomeTimer = { navigateToTab(Screen.Home.route) },
                        onNavigateToChat = { navigateToTab(Screen.Chat.route) },
                        onNavigateToBrowser = { navigateToTab(Screen.Browser.route) }
                    )
                }
                composable(Screen.Chat.route) {
                    DentallyChatScreen(viewModel = viewModel)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) }
                    )
                }
                composable(Screen.Developer.route) {
                    DeveloperDashboardScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Celebratory Confetti Overlay (non-blocking)
        ConfettiBurstOverlay(triggerKey = celebrationTrigger)

        // First-time / On-Demand Interactive Demo Onboarding
        if (showOnboardingDemo) {
            OnboardingDemoDialog(
                onFinish = { viewModel.dismissOnboardingDemo() }
            )
        }

        // Mandatory Evening Check-In Gate (when opened in evening or via evening notification)
        val todayGoodHabits = todayHabits.filter { !it.isBadHabit }
        if (showEveningCheckInGate && todayGoodHabits.isNotEmpty() && !showOnboardingDemo) {
            EveningCheckInGateDialog(
                goodHabits = todayGoodHabits,
                onToggleHabit = { viewModel.toggleHabit(it) },
                onStartBrushingTimer = {
                    viewModel.dismissEveningGateTemporarilyForTimer()
                    navigateToTab(Screen.Home.route)
                    viewModel.startTimer()
                },
                onCompleteAllAndContinue = { viewModel.completeEveningGateIfDone() }
            )
        }
    }
}
