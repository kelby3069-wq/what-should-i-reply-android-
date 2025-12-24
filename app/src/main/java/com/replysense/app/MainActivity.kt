package com.replysense.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.replysense.app.ui.AppRoot
import com.replysense.app.ui.theme.ReplySenseTheme
import com.replysense.app.vm.AppViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReplySenseTheme {
                val vm: AppViewModel = viewModel()
                extractSharedText(intent)?.let { vm.applyIncomingTextOnce(it) }
                AppRoot(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        extractSharedText(intent)?.let {
            // If user shares while app is already open, we replace the current thread with shared content
            val vm: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            vm.applyIncomingTextForce(it)
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
