package com.replysense.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.replysense.app.net.ConversationTurn
import com.replysense.app.vm.AppViewModel
import com.replysense.app.vm.Preset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComposeScreen(vm: AppViewModel, paddingValues: PaddingValues) {
    val s = vm.state.collectAsState().value
    val context = LocalContext.current
    val snack = remember { SnackbarHostState() }

    var pasteBlock by remember { mutableStateOf("") }

    LaunchedEffect(s.error) {
        if (!s.error.isNullOrBlank()) snack.showSnackbar(s.error!!)
    }

    Column(Modifier.padding(paddingValues)) {
        TopAppBar(
            title = { Text("ReplySense") },
            actions = { TextButton(onClick = { vm.clearAll() }) { Text("Reset") } }
        )

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Text("Smart extract", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Paste a whole convo block (with “Name: message” lines). Tap Extract to split into a thread.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item {
                OutlinedTextField(
                    value = pasteBlock,
                    onValueChange = { pasteBlock = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Paste conversation block") },
                    placeholder = { Text("Me: hey\nThem: sup\nMe: you free later?") }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { vm.smartExtract(pasteBlock) },
                        enabled = pasteBlock.isNotBlank()
                    ) { Text("Extract → Thread") }

                    TextButton(
                        onClick = { pasteBlock = "" },
                        enabled = pasteBlock.isNotBlank()
                    ) { Text("Clear") }
                }
            }

            item { Divider() }

            item {
                Text("Thread", style = MaterialTheme.typography.titleMedium)
                Text("Add messages in order. More context = better replies.", style = MaterialTheme.typography.bodySmall)
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
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Preset.values().forEach { p ->
                        FilterChip(
                            selected = s.preset == p,
                            onClick = { vm.setPreset(p) },
                            label = { Text(p.label) }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Presets steer the vibe. Controls below override if you change them.", style = MaterialTheme.typography.bodySmall)
            }

            item { Divider() }

            item {
                Text("Controls", style = MaterialTheme.typography.titleMedium)
                Text("Everything defaults to Auto. Only touch what you want to force.", style = MaterialTheme.typography.bodySmall)
            }

            item {
                SettingDropdown(
                    label = "Variants",
                    value = s.variants.toString(),
                    options = listOf("1","2","3","4","5","6")
                ) { vm.setVariants(it.toInt()) }
            }

            item {
                SettingDropdown(
                    label = "Vibe",
                    value = s.vibe,
                    options = listOf("auto","friendly","playful","serious","flirty","supportive","confident","neutral")
                ) { vm.setVibe(it) }
            }

            item {
                SettingDropdown(
                    label = "Tone",
                    value = s.tone,
                    options = listOf("auto","casual","playful","serious","flirty","supportive","confident","neutral")
                ) { vm.setTone(it) }
            }

            item {
                SettingDropdown(
                    label = "Writing style",
                    value = s.writingStyle,
                    options = listOf("auto","clean","casual","loose","messy")
                ) { vm.setWritingStyle(it) }
            }

            item {
                SettingDropdown(
                    label = "Text quality",
                    value = s.textQuality,
                    options = listOf("auto","perfect","normal","mediocre","rough")
                ) { vm.setTextQuality(it) }
            }

            item {
                SettingDropdown(
                    label = "Emoji level",
                    value = s.emojiLevel,
                    options = listOf("auto","0","1","2","3")
                ) { vm.setEmojiLevel(it) }
            }

            item {
                SettingDropdown(
                    label = "Punctuation",
                    value = s.punctuationPreference,
                    options = listOf("auto","none","light","normal","proper")
                ) { vm.setPunctuationPreference(it) }
            }

            item {
                SettingDropdown(
                    label = "Spice level",
                    value = s.spiceLevel,
                    options = listOf("auto","0","1","2","3")
                ) { vm.setSpiceLevel(it) }
            }

            item {
                SettingDropdown(
                    label = "Age",
                    value = s.age,
                    options = listOf("auto","15","18","21","25","30","35","40","45")
                ) { vm.setAge(it) }
            }

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
                    Text("Tap Copy or Share. Runs auto-save to History.", style = MaterialTheme.typography.bodySmall)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = value,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
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
