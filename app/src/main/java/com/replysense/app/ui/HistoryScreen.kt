package com.replysense.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.db.ReplyHistoryEntity

/**
 * MVP placeholder history screen. Keeps compile green.
 * (We can wire it to Room + ViewModel after APK is building.)
 */
@Composable
fun HistoryScreen(
    history: List<ReplyHistoryEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineSmall
        )

        if (history.isEmpty()) {
            Text("No history yet.")
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(history) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Message: ${item.message}", style = MaterialTheme.typography.bodyMedium)
                        Text("Picked: ${item.selectedReply}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
