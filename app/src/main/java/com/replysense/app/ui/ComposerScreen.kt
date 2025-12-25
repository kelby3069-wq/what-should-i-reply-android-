package com.replysense.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.model.ConversationTurn
import com.replysense.app.vm.AppViewModel

@Composable
fun ComposerScreen(
    vm: AppViewModel,
    modifier: Modifier = Modifier
) {
    val state by vm.state.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ReplySense", style = MaterialTheme.typography.headlineSmall)

        VibePicker(
            vibeOverride = state.vibeOverride,
            onChange = { vm.setVibeOverride(it) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.inputText,
            onValueChange = { vm.setInputText(it) },
            label = { Text("Paste the message you need to reply to") },
            minLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = !state.isLoading && state.inputText.trim().isNotBlank(),
                onClick = {
                    vm.addTurn(ConversationTurn.From.USER, state.inputText.trim())
                    vm.requestReplies()
                }
            ) {
                Text(if (state.isLoading) "Thinking…" else "Generate replies")
            }

            TextButton(
                enabled = !state.isLoading,
                onClick = { vm.clearConversation() }
            ) {
                Text("Clear")
            }
        }

        state.error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.suggestions.isNotEmpty()) {
            Text("Options", style = MaterialTheme.typography.titleMedium)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.suggestions) { suggestion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(suggestion, style = MaterialTheme.typography.bodyLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                TextButton(onClick = {
                                    // treat selecting a suggestion as "OTHER" speaking to user
                                    vm.addTurn(ConversationTurn.From.OTHER, suggestion)
                                }) {
                                    Text("Select")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VibePicker(
    vibeOverride: String?,
    onChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // null = auto
    val label = vibeOverride ?: "Auto vibe (default)"

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Vibe", style = MaterialTheme.typography.titleSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { expanded = true }) {
                Text(label)
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Auto vibe (default)") },
                    onClick = {
                        expanded = false
                        onChange(null)
                    }
                )
                listOf("Chill", "Confident", "Flirty", "Funny", "Serious", "Professional").forEach { vibe ->
                    DropdownMenuItem(
                        text = { Text(vibe) },
                        onClick = {
                            expanded = false
                            onChange(vibe)
                        }
                    )
                }
            }
        }

        Text(
            text = if (vibeOverride == null) "Auto vibe is ON."
            else "Override set: $vibeOverride",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
