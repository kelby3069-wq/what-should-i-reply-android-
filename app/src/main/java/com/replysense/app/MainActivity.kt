package com.replysense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.theme.ReplySenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lastCrash = CrashStore.read(this)

        setContent {
            ReplySenseTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (lastCrash != null) {
                        CrashReportScreen(
                            crash = lastCrash,
                            onClear = {
                                CrashStore.clear(this@MainActivity)
                                recreate()
                            }
                        )
                    } else {
                        // ✅ Minimal safe “boot” UI so you can confirm the app opens.
                        // Replace this with your real navigation once stable.
                        BootScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun BootScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ReplySense", style = MaterialTheme.typography.headlineMedium)
        Text(
            "App opened successfully.\nIf it previously crashed, you’d see a crash report screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Next", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Now that launch is stable, we wire your real flow back in (pick → crop → OCR → reply) behind a crash-safe guard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CrashReportScreen(
    crash: String,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crash Report", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This is why the app instantly closes when you tap it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = crash,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onClear) { Text("Clear & Relaunch") }
        }
    }
}
