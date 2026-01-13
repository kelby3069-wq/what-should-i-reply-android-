package com.replysense.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.components.GlassCard
import com.replysense.app.ui.components.GlassPillButton

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Welcome to ReplySense",
            style = MaterialTheme.typography.titleLarge
        )

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• ReplySense helps you understand conversations.")
                Text("• It doesn’t auto-reply or tell you what to do.")
                Text("• Confidence levels show how clear signals are.")
                Text("• Sometimes, not replying is the healthiest option.")
            }
        }

        GlassPillButton(
            label = "Get Started",
            onClick = onFinish
        )
    }
}
