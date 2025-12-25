package com.replysense.app.db

/**
 * ReplySense baseline: NO database layer yet.
 *
 * We intentionally do not include Room/DataStore until the build is stable.
 * This file exists so any legacy references compile during the baseline phase.
 *
 * When you add persistence, we’ll replace this with either:
 * - Room (AppDatabase : RoomDatabase + @Database)
 * - DataStore (no database class needed)
 */
object AppDatabase {

    /**
     * Placeholder accessors so older code can compile if it expects a DAO.
     * Returns null in baseline builds.
     */
    fun replyHistoryDaoOrNull(): ReplyHistoryDao? = null
}
