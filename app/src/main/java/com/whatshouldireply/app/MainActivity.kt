package com.whatshouldireply.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import kotlinx.coroutines.launch

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

private enum class Tone(val label: String) {
    CALM("Calm & Polite"),
    FIRM("Firm but Respectful"),
    PROFESSIONAL("Professional"),
    SHORT("Short & Direct"),
    APOLOGETIC("Apologetic"),
    ASSERTIVE("Assertive"),
    NO_DRAMA("Don’t Start Drama"),
    FLIRTY("Flirty")
}

@Composable
private fun Home(sharedImageUriState: MutableState<Uri?>) {
    val scope = rememberCoroutineScope()
    val ctx = androidx.compose.ui.platform.LocalContext.current

    var ocrBusy by remember { mutableStateOf(false) }
    var aiBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var prompt by remember { mutableStateOf("") }
    var targetPreview by remember { mutableStateOf<String?>(null) }

    var selectedTone by remember { mutableStateOf(Tone.CALM) }
    var goal by remember { mutableStateOf("") }

    var replies by remember { mutableStateOf(listOf<String>()) }

    fun copyToClipboard(text: String) {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("reply", text))
    }

    // OCR on incoming shared screenshot
    LaunchedEffect(sharedImageUriState.value) {
        val uri = sharedImageUriState.value ?: return@LaunchedEffect
        ocrBusy = true
        error = null
        prompt = ""
        targetPreview = null
        replies = emptyList()

        try {
            val convo = OcrUtil.extractConversation(ctx, uri)
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
            ocrBusy = false
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

        if (ocrBusy) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
        }

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(10.dp))
        }

        if (targetPreview != null) {
            Text("Detected last message:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            SelectionContainer { Text(targetPreview!!, style = MaterialTheme.typography.bodyLarge) }
            Spacer(Modifier.height(12.dp))
        }

        Text("Extracted context (editable)", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 7,
            placeholder = { Text("Share screenshot to auto-fill…") }
        )

        Spacer(Modifier.height(12.dp))

        Text("Goal (optional)", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Example: keep it flirty but not cringe") }
        )

        Spacer(Modifier.height(12.dp))

        Text("Tone", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        TonePicker(selectedTone = selectedTone, onPick = { selectedTone = it })

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                enabled = prompt.isNotBlank() && !ocrBusy && !aiBusy,
                onClick = {
                    error = null
                    replies = emptyList()
                    aiBusy = true

                    scope.launch {
                        try {
                            val resp = ApiClient.generateReplies(
                                fullContextPrompt = prompt.trim(),
                                toneLabel = selectedTone.label,
                                goal = goal.trim().ifBlank { null }
                            )
                            replies = resp.replies
                        } catch (t: Throwable) {
                            error = t.message ?: "AI request failed."
                        } finally {
                            aiBusy = false
                        }
                    }
                }
            ) {
                Text(if (aiBusy) "Generating…" else "Generate Replies")
            }

            OutlinedButton(
                enabled = !ocrBusy && !aiBusy,
                onClick = {
                    error = null
                    prompt = ""
                    targetPreview = null
                    goal = ""
                    replies = emptyList()
                }
            ) { Text("Clear") }
        }

        if (aiBusy) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        if (replies.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Replies:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            replies.forEachIndexed { idx, r ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Option ${idx + 1}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        SelectionContainer { Text(r, style = MaterialTheme.typography.bodyLarge) }
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { copyToClipboard(r) }) { Text("Copy") }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Next: credits + paywall. (AI is now live via your proxy.)", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TonePicker(selectedTone: Tone, onPick: (Tone) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Tone.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { t ->
                    FilterChip(
                        selected = selectedTone == t,
                        onClick = { onPick(t) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    }
}
