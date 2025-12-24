package com.replysense.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reply_history")
data class ReplyHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtMs: Long,
    val isFavorite: Boolean,
    val conversationJson: String,
    val requestJson: String,
    val responseJson: String
)
