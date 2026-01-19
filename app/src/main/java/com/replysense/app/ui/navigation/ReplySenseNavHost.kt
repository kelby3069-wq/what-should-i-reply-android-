package com.replysense.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.replysense.app.ui.input.InputScreen
import com.replysense.app.ui.review.AnalysisScreen
import com.replysense.app.viewmodel.AnalysisViewModel

@Composable
fun ReplySenseNavHost(
    navController: NavHostController,
    analysisViewModel: AnalysisViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "input"
    ) {
        composable("input") {
            InputScreen(
                analysisViewModel = analysisViewModel,
                onAnalyzeComplete = {
                    navController.navigate("analysis")
                }
            )
        }

        composable("analysis") {
            AnalysisScreen(
                analysisViewModel = analysisViewModel
            )
        }
    }
}
