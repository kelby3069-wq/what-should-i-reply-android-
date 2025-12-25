package com.replysense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.replysense.app.ui.theme.ReplySenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReplySenseTheme {
                // TODO: Replace AppRoot() with your existing root composable
                // Examples you might already have: App(), MainScreen(), ReplySenseScreen(), NavGraph()
                AppRoot()
            }
        }
    }
}

/**
 * Temporary adapter so MainActivity compiles even if your root composable name is different.
 * Replace the body with your real root composable call and delete this function after.
 */
@androidx.compose.runtime.Composable
private fun AppRoot() {
    // Replace this line with YOUR app's real root composable:
    // ReplySenseApp()
    // MainScreen()
    // AppNavHost()
}
