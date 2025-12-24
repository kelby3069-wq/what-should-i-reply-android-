package com.replysense.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.replysense.app.ui.ComposerScreen
import com.replysense.app.ui.theme.ReplySenseTheme
import com.replysense.app.vm.ComposerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText: String? = extractSharedText(intent)

        setContent {
            ReplySenseTheme {
                val vm: ComposerViewModel = viewModel()
                // Apply shared text once
                if (!sharedText.isNullOrBlank()) vm.applyIncomingTextOnce(sharedText)
                ComposerScreen(vm = vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val sharedText = extractSharedText(intent)
        if (!sharedText.isNullOrBlank()) {
            // If app already open and user shares again
            (this as ComponentActivity).setContent {
                ReplySenseTheme {
                    val vm: ComposerViewModel = viewModel()
                    vm.applyIncomingTextOnce(sharedText)
                    ComposerScreen(vm = vm)
                }
            }
        }
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent == null) return null
        if (intent.action != Intent.ACTION_SEND) return null
        val type = intent.type ?: return null
        if (!type.startsWith("text/")) return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
    }
}
