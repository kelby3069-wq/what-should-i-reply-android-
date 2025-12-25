package com.replysense.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.replysense.app.network.ReplyOption
import com.replysense.app.network.ReplyRequest
import com.replysense.app.network.ReplySenseApi
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ReplySenseScreen() }
    }
}

private data class VibeChoice(val label: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun ReplySenseScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    val api = remember { ReplySenseApi() }

    var input by remember { mutableStateOf("") }
    var context by remember { mutableStateOf("") }

    // ✅ Auto is the default. If user picks anything else, it becomes manual automatically.
    val vibeChoices = remember {
        listOf(
            VibeChoice("Auto (recommended)", "auto"),
            VibeChoice("Neutral", "neutral"),
            VibeChoice("Friendly", "friendly"),
            VibeChoice("Flirty", "flirty"),
            VibeChoice("Professional", "professional"),
            VibeChoice("Apology", "apology"),
            VibeChoice("Tough-love", "tough-love"),
            VibeChoice("Troll", "troll")
        )
    }

    var selectedVibe by remember { mutableStateOf(vibeChoices.first()) }
    var vibeMenuOpen by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf<List<ReplyOption>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(error) {
        error?.let { snack.showSnackbar(it) }
        error = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReplySense", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior(
                    rememberTopAppBarState()
                )
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Paste the message / thread") },
                minLines = 4
            )

            OutlinedTextField(
                value = context,
                onValueChange = { context = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Optional context (who/what/goal)") },
                minLines = 2
            )

            // Single dropdown with Auto as the first/default choice
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Vibe", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vibeMenuOpen = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedVibe.label)
                    }
                }

                DropdownMenu(expanded = vibeMenuOpen, onDismissRequest = { vibeMenuOpen = false }) {
                    vibeChoices.forEach { choice ->
                        DropdownMenuItem(
                            text = { Text(choice.label) },
                            onClick = {
                                selectedVibe = choice
                                vibeMenuOpen = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (input.isBlank()) {
                        error = "Paste something first."
                        return@Button
                    }
                    loading = true
                    options = emptyList()

                    scope.launch {
                        try {
                            val req = ReplyRequest(
                                text = input.trim(),
                                vibe = selectedVibe.value, // "auto" or manual vibe
                                context = context.trim().ifBlank { null },
                                platform = "android"
                            )

                            val resp = api.generateReplies(req)
                            val list = resp.allOptions()
                            if (list.isEmpty()) {
                                error = "No options returned. (Parsed OK, empty list.)"
                            }
                            options = list
                        } catch (t: Throwable) {
                            error = t.message ?: "Request failed"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.height(18.dp))
                else Text("Generate")
            }

            Divider()

            Text("Tap an option to copy.", style = MaterialTheme.typography.labelMedium)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(options) { index, opt ->
                    ReplyCard(
                        index = index,
                        option = opt,
                        onCopy = { text ->
                            copyToClipboard(ctx, text)
                            scope.launch { snack.showSnackbar("Copied.") }
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ReplyCard(index: Int, option: ReplyOption, onCopy: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy(option.text) }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Option ${index + 1}", style = MaterialTheme.typography.labelLarge)
            option.label?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
            option.vibe?.let { Text("Vibe: $it", style = MaterialTheme.typography.labelSmall) }
            Text(option.text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("reply", text))
}
