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
                ReplySenseApp() // whatever your root composable is
            }
        }
    }
}
