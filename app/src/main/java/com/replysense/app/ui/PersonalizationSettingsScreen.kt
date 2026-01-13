package com.replysense.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.data.prefs.UserPreferencesDataStore
import com.replysense.app.domain.prefs.*
import kotlinx.coroutines.launch

@Composable
fun PersonalizationSettingsScreen(
    prefsStore: UserPreferencesDataStore,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var tone by remember { mutableStateOf(CoachingTone.BALANCED) }
    var depth by remember { mutableStateOf(ExplanationDensity.STANDARD) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Personalization", style = MaterialTheme.typography.titleLarge)

        Text("Coaching tone")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CoachingTone.values().forEach {
                FilterChip(
                    selected = tone == it,
                    onClick = { tone = it },
                    label = { Text(it.name) }
                )
            }
        }

        Text("Explanation depth")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExplanationDensity.values().forEach {
                FilterChip(
                    selected = depth == it,
                    onClick = { depth = it },
                    label = { Text(it.name) }
                )
            }
        }

        Button(onClick = {
            scope.launch {
                prefsStore.optInAndSave(tone, depth)
                onDone()
            }
        }) {
            Text("Save preferences")
        }

        OutlinedButton(onClick = {
            scope.launch {
                prefsStore.revokeConsent()
                onDone()
            }
        }) {
            Text("Revoke personalization")
        }
    }
}
