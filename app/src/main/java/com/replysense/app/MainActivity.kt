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
import androidx.compose.material3.Switch
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

private enum class VibeMode { AUTO, MANUAL }

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun ReplySenseScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    val api = remember { ReplySenseApi() }

    var input by remember { mutableStateOf("") }
    var context by remember { mutableStateOf("") }

    val vibes = remember {
        listOf("neutral", "friendly", "flirty", "professional", "apology", "tough-love", "troll")
    }

    // ✅ Auto-vibe default
    var vibeMode by remember { mutableStateOf(VibeMode.AUTO) }
    var selectedVibe by remember { mutableStateOf(vibes.first()) }
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

            // Vibe mode row
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibe", style = MaterialTheme.typography.labelLarge)
                        Text(
                            if (vibeMode == VibeMode.AUTO) "AUTO (Worker decides)" else "MANUAL (you pick)",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Switch(
                        checked = (vibeMode == VibeMode.MANUAL),
                        onCheckedChange = { checked ->
                            vibeMode = if (checked) VibeMode.MANUAL else VibeMode.AUTO
                        }
                    )
                }

                if (vibeMode == VibeMode.MANUAL) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                            .fillMaxWidth()
                            .clickable { vibeMenuOpen = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedVibe)
                        }
                    }

                    DropdownMenu(expanded = vibeMenuOpen, onDismissRequest = { vibeMenuOpen = false }) {
                        vibes.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(v) },
                                onClick = {
                                    selectedVibe = v
                                    vibeMenuOpen = false
                                }
                            )
                        }
                    }
                } else {
                    // In AUTO mode, show allowed vibes (optional hint)
                    Text(
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                        text = "Auto can choose from: ${vibes.joinToString()}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                                val vibeToSend =
                                    if (vibeMode == VibeMode.AUTO) "auto" else selectedVibe

                                val req = ReplyRequest(
                                    text = input.trim(),
                                    vibe = vibeToSend,
                                    context = context.trim().ifBlank { null },
                                    platform = "android",
                                    vibesAllowed = if (vibeMode == VibeMode.AUTO) vibes else null
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
                    modifier = Modifier.weight(1f)
                ) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.height(18.dp))
                    else Text("Generate")
                }
            }

            Divider()

            Text(
                "Tap an option to copy.",
                style = MaterialTheme.typography.labelMedium
            )

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
