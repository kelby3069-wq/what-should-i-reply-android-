package com.replysense.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.replysense.app.ui.input.InputScreen
import com.replysense.app.ui.review.AnalysisScreen

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.INPUT
    ) {
        composable(NavRoutes.INPUT) {
            InputScreen()
        }
        composable(NavRoutes.ANALYSIS) {
            AnalysisScreen()
        }
    }
}
