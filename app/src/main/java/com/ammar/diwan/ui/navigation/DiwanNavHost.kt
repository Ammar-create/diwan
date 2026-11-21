package com.ammar.diwan.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.data.preferences.PreferencesManager
import com.ammar.diwan.ui.chat.PoetSalonScreen
import com.ammar.diwan.ui.chat.PoetSalonViewModel
import com.ammar.diwan.ui.dashboard.DashboardScreen
import com.ammar.diwan.ui.dashboard.DashboardViewModel
import com.ammar.diwan.ui.onboarding.OnboardingScreen
import com.ammar.diwan.ui.onboarding.OnboardingViewModel
import com.ammar.diwan.ui.settings.SettingsScreen
import com.ammar.diwan.ui.settings.SettingsViewModel
import com.ammar.diwan.ui.theme.DiwanMotion

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
            slideInHorizontally(DiwanMotion.FluidSpatialInt) { it / 4 } +
                fadeIn(DiwanMotion.enterFade(240))
        },
        exitTransition = {
            slideOutHorizontally(tween(180)) { -it / 6 } +
                fadeOut(DiwanMotion.exitFade(140))
        },
        popEnterTransition = {
            slideInHorizontally(DiwanMotion.FluidSpatialInt) { -it / 4 } +
                fadeIn(DiwanMotion.enterFade(240))
        },
        popExitTransition = {
            slideOutHorizontally(tween(180)) { it / 6 } +
                fadeOut(DiwanMotion.exitFade(140))
        }
    ) {
        composable(
            route = Destinations.ONBOARDING,
            exitTransition = {
                fadeOut(tween(240)) + scaleOut(targetScale = 1.04f)
            }
        ) {
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

        composable(
            route = Destinations.DASHBOARD,
            enterTransition = {
                if (initialState.destination.route == Destinations.ONBOARDING) {
                    fadeIn(tween(280)) + scaleIn(initialScale = 0.96f)
                } else {
                    slideInHorizontally(DiwanMotion.FluidSpatialInt) { it / 4 } +
                        fadeIn(DiwanMotion.enterFade(240))
                }
            }
        ) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToScan = { navController.navigate(Destinations.CHAT) }, // Redirected to Unified Chat Sanctuary
                onNavigateToChat = { navController.navigate(Destinations.CHAT) },
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }

        // Standalone OCR route redirected directly to the Unified Chat Sanctuary
        composable(Destinations.SCAN) {
            val salonVm = remember { PoetSalonViewModel(preferencesManager) }
            PoetSalonScreen(
                viewModel = salonVm,
                onNavigateBack = { navController.popBackStack() },
                onPoemSavedToFolio = { poem: Poem ->
                    dashboardViewModel.addPoemToFolio(poem)
                },
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }

        composable(Destinations.CHAT) {
            val salonVm = remember { PoetSalonViewModel(preferencesManager) }
            PoetSalonScreen(
                viewModel = salonVm,
                onNavigateBack = { navController.popBackStack() },
                onPoemSavedToFolio = { poem: Poem ->
                    dashboardViewModel.addPoemToFolio(poem)
                },
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) }
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
