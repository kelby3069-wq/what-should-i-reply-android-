package com.replysense.app.ui.personalization

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.domain.prefs.*
import com.replysense.app.viewmodel.ui.PersonalizationConsentViewModel

@Composable
fun PersonalizationConsentScreen(
    vm: PersonalizationConsentViewModel,
    onDone: () -> Unit
) {
    val tone by vm.selectedTone.collectAsState()
    val density by vm.selectedDensity.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personalize delivery (optional)",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "This only changes how guidance is worded. " +
                    "Analysis, responsibility, red flags, and recoverability never change.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text("Coaching tone", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CoachingTone.values().forEach {
                FilterChip(
                    selected = tone == it,
                    onClick = { vm.setTone(it) },
                    label = { Text(it.name.lowercase().replaceFirstChar(Char::uppercase)) }
                )
            }
        }

        Text("Explanation depth", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExplanationDensity.values().forEach {
                FilterChip(
                    selected = density == it,
                    onClick = { vm.setDensity(it) },
                    label = { Text(it.name.lowercase().replaceFirstChar(Char::uppercase)) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { vm.decline(); onDone() },
                modifier = Modifier.weight(1f)
            ) { Text("Skip") }

            Button(
                onClick = { vm.confirmOptIn(); onDone() },
                modifier = Modifier.weight(1f)
            ) { Text("Save preferences") }
        }
    }
}
