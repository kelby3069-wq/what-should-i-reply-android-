package com.replysense.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reply_history")
data class ReplyHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val createdAtEpochMs: Long,
    val message: String,
    val selectedReply: String
)
