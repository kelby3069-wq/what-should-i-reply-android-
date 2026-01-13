package com.replysense.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavShell() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "input"
    ) {
        // routes defined elsewhere
    }
}
