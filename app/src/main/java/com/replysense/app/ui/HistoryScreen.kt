package com.replysense.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.db.ReplyHistoryEntity
import com.replysense.app.net.Api
import com.replysense.app.net.ConversationTurn
import com.replysense.app.net.ReplyRequest
import com.replysense.app.net.ReplyResponse
import com.replysense.app.vm.AppViewModel
import com.replysense.app.vm.DecodedHistory
import kotlinx.serialization.builtins.ListSerializer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: AppViewModel, paddingValues: PaddingValues) {
    val s = vm.state.collectAsState().value

    Column(Modifier.padding(paddingValues)) {
        TopAppBar(
            title = { Text("History") },
            actions = {
                FilterChip(
                    selected = s.favoritesOnly,
                    onClick = { vm.setFavoritesOnly(!s.favoritesOnly) },
                    label = { Text(if (s.favoritesOnly) "Favorites" else "All") }
                )
                Spacer(Modifier.padding(6.dp))
            }
        )

        if (s.history.isEmpty()) {
            Column(Modifier.padding(16.dp)) {
                Text("No history yet.", style = MaterialTheme.typography.titleMedium)
                Text("Generate replies and they’ll auto-save here.", style = MaterialTheme.typography.bodySmall)
            }
            return
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(s.history) { item ->
                HistoryCard(
                    item = item,
                    onLoad = {
                        val decoded = decode(item)
                        if (decoded != null) vm.loadHistoryItem(item, decoded)
                    },
                    onFav = { vm.toggleFavorite(item) },
                    onDelete = { vm.deleteHistory(item) }
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: ReplyHistoryEntity,
    onLoad: () -> Unit,
    onFav: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = if (item.isFavorite) "★ Saved" else "Saved",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onLoad) { Text("Open") }
                TextButton(onClick = onFav) { Text(if (item.isFavorite) "Unfavorite" else "Favorite") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

private fun decode(e: ReplyHistoryEntity): DecodedHistory? {
    return runCatching {
        val conv = Api.json.decodeFromString(
            ListSerializer(ConversationTurn.serializer()),
            e.conversationJson
        )
        val req = Api.json.decodeFromString(ReplyRequest.serializer(), e.requestJson)
        val resp = Api.json.decodeFromString(ReplyResponse.serializer(), e.responseJson)
        DecodedHistory(conv, req, resp)
    }.getOrNull()
}
