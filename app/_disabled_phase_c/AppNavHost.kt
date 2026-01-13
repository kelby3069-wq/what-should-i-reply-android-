package com.replysense.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.replysense.app.ui.home.HomeScreen
import com.replysense.app.ui.input.InputScreen
import com.replysense.app.ui.settings.PaywallScreen
import com.replysense.app.ui.onboarding.OnboardingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController, startDestination = startDestination) {

        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(navController)
        }

        composable(NavRoutes.INPUT) {
            InputScreen(navController)
        }

        composable(NavRoutes.PAYWALL) {
            PaywallScreen(
                onUpgrade = {
                    // Billing launched from Activity
                },
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
    }
}
