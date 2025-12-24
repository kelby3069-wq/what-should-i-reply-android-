package com.whatshouldireply.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val sharedImageUriState = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent(intent)
        setContent { App(sharedImageUriState) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            sharedImageUriState.value = uri
        }
    }
}

@Composable
private fun App(sharedImageUriState: MutableState<Uri?>) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(Modifier.fillMaxSize()) {
            Home(sharedImageUriState)
        }
    }
}

@Composable
private fun Home(sharedImageUriState: MutableState<Uri?>) {
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var prompt by remember { mutableStateOf("") }
    var targetPreview by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sharedImageUriState.value) {
        val uri = sharedImageUriState.value ?: return@LaunchedEffect
        busy = true
        error = null
        prompt = ""
        targetPreview = null

        try {
            val convo = OcrUtil.extractConversation(this@Home, uri)
            val target = convo.target
            if (convo.chunks.isEmpty() || target == null) {
                error = "Couldn’t read the conversation. Zoom in and try again."
            } else {
                targetPreview = target.text
                prompt = OcrUtil.buildPrompt(convo)
            }
        } catch (t: Throwable) {
            error = t.message ?: "OCR failed."
        } finally {
            busy = false
            sharedImageUriState.value = null
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("What Should I Reply?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text("Share a screenshot → we read everything visible and reply to the LAST message.")

        Spacer(Modifier.height(12.dp))

        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (targetPreview != null) {
            Spacer(Modifier.height(12.dp))
            Text("Detected last message:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            SelectionContainer { Text(targetPreview!!, style = MaterialTheme.typography.bodyLarge) }
        }

        Spacer(Modifier.height(12.dp))
        Text("Extracted context (editable)", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 7,
            placeholder = { Text("Share screenshot to auto-fill…") }
        )
    }
}
