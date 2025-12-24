package com.replysense.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.foundation.layout.PaddingValues
import com.replysense.app.net.ConversationTurn
import com.replysense.app.vm.AppViewModel
import com.replysense.app.vm.Preset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(vm: AppViewModel, paddingValues: PaddingValues) {
    val s = vm.state.collectAsState().value
    val context = LocalContext.current
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(s.error) {
        if (!s.error.isNullOrBlank()) snack.showSnackbar(s.error!!)
    }

    Column(Modifier.padding(paddingValues)) {
        TopAppBar(
            title = { Text("ReplySense") },
            actions = {
                TextButton(onClick = { vm.clearAll() }) { Text("Reset") }
            }
        )

        LazyColumn(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Text("Thread", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Add messages in order. This is the biggest quality jump you can make.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            itemsIndexed(s.turns) { idx, turn ->
                TurnEditor(
                    index = idx,
                    turn = turn,
                    onFromChange = { vm.updateTurn(idx, from = it) },
                    onTextChange = { vm.updateTurn(idx, text = it) },
                    onRemove = { vm.removeTurn(idx) }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { vm.addTurn("them") }) { Text("+ Them") }
                    Button(onClick = { vm.addTurn("me") }) { Text("+ Me") }
                }
            }

            item { Divider() }

            item {
                Text("Preset", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Preset.values().fastForEach { p ->
                        FilterChip(
                            selected = s.preset == p,
                            onClick = { vm.setPreset(p) },
                            label = { Text(p.label) }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Presets nudge tone/clarity without killing auto vibe. You can still override below.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item { Divider() }

            item {
                Text("Auto vibe controls (leave as auto if you want it to “feel out” the vibe)", style = MaterialTheme.typography.titleMedium)
            }

            item { AutoField("variants (1–6)", s.variants.toString()) { vm.setVariants(it.toIntOrNull() ?: 3) } }
            item { AutoField("vibe", s.vibe, vm::setVibe) }
            item { AutoField("tone", s.tone, vm::setTone) }
            item { AutoField("writingStyle", s.writingStyle, vm::setWritingStyle) }
            item { AutoField("textQuality", s.textQuality, vm::setTextQuality) }
            item { AutoField("emojiLevel", s.emojiLevel, vm::setEmojiLevel) }
            item { AutoField("spiceLevel", s.spiceLevel, vm::setSpiceLevel) }
            item { AutoField("age", s.age, vm::setAge) }
            item { AutoField("punctuationPreference", s.punctuationPreference, vm::setPunctuationPreference) }

            item {
                Button(
                    onClick = { vm.generate() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !s.isLoading
                ) {
                    if (s.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp))
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
                    Text(
                        "Tap Copy or Share. Everything is auto-saved to History.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                itemsIndexed(s.replies) { idx, reply ->
                    ReplyCard(
                        index = idx + 1,
                        text = reply,
                        onCopy = { copyToClipboard(context, reply) },
                        onShare = { shareText(context, reply) }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        SnackbarHost(hostState = snack)
    }
}

@Composable
private fun TurnEditor(
    index: Int,
    turn: ConversationTurn,
    onFromChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = turn.from == "them",
                    onClick = { onFromChange("them") },
                    label = { Text("Them") }
                )
                FilterChip(
                    selected = turn.from == "me",
                    onClick = { onFromChange("me") },
                    label = { Text("Me") }
                )

                Spacer(Modifier.weight(1f))
                TextButton(onClick = onRemove) { Text("Remove") }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = turn.text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Message #${index + 1}") }
            )
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
        placeholder = { Text("auto", fontSize = 12.sp) }
    )
}

@Composable
private fun ReplyCard(index: Int, text: String, onCopy: () -> Unit, onShare: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Option $index", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text(text)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onCopy) { Text("Copy") }
                TextButton(onClick = onShare) { Text("Share") }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("ReplySense", text))
}

private fun shareText(context: Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(i, "Send reply"))
}
