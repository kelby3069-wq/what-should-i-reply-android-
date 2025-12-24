package com.replysense.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.replysense.app.vm.ComposerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerScreen(vm: ComposerViewModel) {
    val s = vm.state.collectAsState().value
    val context = LocalContext.current
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(s.error) {
        if (!s.error.isNullOrBlank()) snack.showSnackbar(s.error!!)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReplySense") },
                actions = {
                    TextButton(onClick = { vm.clearAll() }) { Text("Reset") }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snack) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Paste what they said (or share to this app).",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                OutlinedTextField(
                    value = s.theirMessage,
                    onValueChange = vm::setTheirMessage,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text("Their message") },
                    placeholder = { Text("e.g. “You as well!! It’s too warm I hate it”") }
                )
            }

            item {
                Divider()
            }

            item {
                Text(
                    "Auto vibe controls (defaults to auto). You can leave these alone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                OutlinedTextField(
                    value = s.variants.toString(),
                    onValueChange = { v -> vm.setVariants(v.toIntOrNull() ?: 3) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Variants (1–6)") }
                )
            }

            item { AutoField("vibe", s.vibe, vm::setVibe) }
            item { AutoField("tone", s.tone, vm::setTone) }
            item { AutoField("writingStyle", s.writingStyle, vm::setWritingStyle) }
            item { AutoField("textQuality", s.textQuality, vm::setTextQuality) }
            item { AutoField("emojiLevel", s.emojiLevel, vm::setEmojiLevel) }
            item { AutoField("spiceLevel", s.spiceLevel, vm::setSpiceLevel) }
            item { AutoField("age", s.age, vm::setAge) }
            item { AutoField("punctuationPreference", s.punctuationPreference, vm::setPunctuationPreference) }

            item {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { vm.generate() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !s.isLoading
                ) {
                    if (s.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp))
                        Spacer(Modifier.height(0.dp))
                        Text("  Generating…")
                    } else {
                        Text("Generate replies")
                    }
                }
            }

            if (!s.detectedNotes.isNullOrBlank()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Auto vibe notes", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            Text(s.detectedNotes!!)
                        }
                    }
                }
            }

            if (s.replies.isNotEmpty()) {
                item {
                    Text("Replies", style = MaterialTheme.typography.titleMedium)
                }
                itemsIndexed(s.replies) { idx, reply ->
                    ReplyCard(
                        index = idx + 1,
                        text = reply,
                        onCopy = { copyToClipboard(context, reply) }
                    )
                }
            } else {
                item {
                    Text(
                        "No replies yet. Tap Generate.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text("auto") }
    )
}

@Composable
private fun ReplyCard(index: Int, text: String, onCopy: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Option $index", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text(text)
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onCopy) { Text("Copy") }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("ReplySense", text))
}
