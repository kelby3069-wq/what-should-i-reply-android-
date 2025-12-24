package com.replysense.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplyHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReplyHistoryEntity): Long

    @Update
    suspend fun update(entity: ReplyHistoryEntity)

    @Query("SELECT * FROM reply_history ORDER BY createdAtMs DESC")
    fun observeAll(): Flow<List<ReplyHistoryEntity>>

    @Query("SELECT * FROM reply_history WHERE isFavorite = 1 ORDER BY createdAtMs DESC")
    fun observeFavorites(): Flow<List<ReplyHistoryEntity>>

    @Query("DELETE FROM reply_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reply_history")
    suspend fun deleteAll()
}
