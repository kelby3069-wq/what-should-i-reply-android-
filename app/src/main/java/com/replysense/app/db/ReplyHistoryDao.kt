package com.replysense.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReplyHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReplyHistoryEntity): Long

    @Query("SELECT * FROM reply_history ORDER BY createdAtEpochMs DESC LIMIT :limit")
    suspend fun latest(limit: Int = 50): List<ReplyHistoryEntity>

    @Query("DELETE FROM reply_history")
    suspend fun clearAll()
}
