package com.replysense.app.repo

import android.content.Context
import com.replysense.app.db.AppDatabase
import com.replysense.app.db.ReplyHistoryEntity
import com.replysense.app.net.Api
import com.replysense.app.net.ConversationTurn
import com.replysense.app.net.ReplyRequest
import com.replysense.app.net.ReplyResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ReplyRepository(context: Context) {
    private val db = AppDatabase.get(context)
    private val dao = db.historyDao()

    fun observeHistory(): Flow<List<ReplyHistoryEntity>> = dao.observeAll()
    fun observeFavorites(): Flow<List<ReplyHistoryEntity>> = dao.observeFavorites()

    suspend fun toggleFavorite(entity: ReplyHistoryEntity) = withContext(Dispatchers.IO) {
        dao.update(entity.copy(isFavorite = !entity.isFavorite))
    }

    suspend fun delete(entity: ReplyHistoryEntity) = withContext(Dispatchers.IO) {
        dao.deleteById(entity.id)
    }

    suspend fun generateAndSave(payload: ReplyRequest): Result<Pair<ReplyResponse, Long>> =
        withContext(Dispatchers.IO) {
            val res = Api.generateReplies(payload)
            res.map { response ->
                val conversationJson = Api.json.encodeToString(
                    ListSerializer(ConversationTurn.serializer()),
                    payload.conversation
                )
                val requestJson = Api.json.encodeToString(ReplyRequest.serializer(), payload)
                val responseJson = Api.json.encodeToString(ReplyResponse.serializer(), response)

                val id = dao.insert(
                    ReplyHistoryEntity(
                        createdAtMs = System.currentTimeMillis(),
                        isFavorite = false,
                        conversationJson = conversationJson,
                        requestJson = requestJson,
                        responseJson = responseJson
                    )
                )
                response to id
            }
        }
}

// helper because kotlinx ListSerializer lives in a different package
private fun <T> ListSerializer(serializer: kotlinx.serialization.KSerializer<T>) =
    kotlinx.serialization.builtins.ListSerializer(serializer)
