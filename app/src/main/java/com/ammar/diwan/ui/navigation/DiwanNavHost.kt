package com.ammar.diwan.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ammar.diwan.data.preferences.PreferencesManager
import com.ammar.diwan.ui.chat.PoetSalonScreen
import com.ammar.diwan.ui.chat.PoetSalonViewModel
import com.ammar.diwan.ui.dashboard.DashboardScreen
import com.ammar.diwan.ui.dashboard.DashboardViewModel
import com.ammar.diwan.ui.ocr.CalligraphyScanScreen
import com.ammar.diwan.ui.ocr.CalligraphyScanViewModel
import com.ammar.diwan.ui.onboarding.OnboardingScreen
import com.ammar.diwan.ui.onboarding.OnboardingViewModel
import com.ammar.diwan.ui.settings.SettingsScreen
import com.ammar.diwan.ui.settings.SettingsViewModel

object Destinations {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val SCAN = "scan"
    const val CHAT = "chat"
    const val SETTINGS = "settings"
}

@Composable
fun DiwanNavHost(preferencesManager: PreferencesManager) {
    val navController = rememberNavController()
    val isSetupCompleted by preferencesManager.isSetupCompleted.collectAsState(initial = null)

    if (isSetupCompleted == null) {
        // Initializing Preferences...
        return
    }

    val startDestination = if (isSetupCompleted == true) {
        Destinations.DASHBOARD
    } else {
        Destinations.ONBOARDING
    }

    val dashboardViewModel = remember { DashboardViewModel(preferencesManager) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { 300 } + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { -300 } + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { -300 } + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { 300 } + fadeOut()
        }
    ) {
        composable(Destinations.ONBOARDING) {
            val onboardingVm = remember { OnboardingViewModel(preferencesManager) }
            OnboardingScreen(
                viewModel = onboardingVm,
                onComplete = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToScan = { navController.navigate(Destinations.SCAN) },
                onNavigateToChat = { navController.navigate(Destinations.CHAT) },
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }

        composable(Destinations.SCAN) {
            val scanVm = remember { CalligraphyScanViewModel(preferencesManager) }
            CalligraphyScanScreen(
                viewModel = scanVm,
                onNavigateBack = { navController.popBackStack() },
                onPoemSaved = { poem ->
                    dashboardViewModel.addScannedPoem(poem)
                }
            )
        }

        composable(Destinations.CHAT) {
            val salonVm = remember { PoetSalonViewModel(preferencesManager) }
            PoetSalonScreen(
                viewModel = salonVm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.SETTINGS) {
            val settingsVm = remember { SettingsViewModel(preferencesManager) }
            SettingsScreen(
                viewModel = settingsVm,
                onNavigateBack = { navController.popBackStack() },
                onRerunOnboarding = {
                    navController.navigate(Destinations.ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
