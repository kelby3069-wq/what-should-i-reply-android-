package com.replysense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.replysense.app.ui.ComposerScreen
import com.replysense.app.vm.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val vm: AppViewModel = viewModel()

            MaterialTheme {
                Surface {
                    ComposerScreen(vm = vm)
                }
            }
        }
    }
}
