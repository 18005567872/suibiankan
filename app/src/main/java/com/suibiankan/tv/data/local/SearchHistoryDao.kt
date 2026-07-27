package com.suibiankan.tv.data.local

import androidx.room.*

/**
 * Data Access Object for search history.
 */
@Dao
interface SearchHistoryDao {

    /**
     * Get recent search history, newest first.
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<SearchHistoryEntity>

    /**
     * Insert a new search query. If it already exists, update its timestamp.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SearchHistoryEntity)

    /**
     * Delete the oldest entries to keep the total count below [keepCount].
     */
    @Query("""
        DELETE FROM search_history WHERE id IN (
            SELECT id FROM search_history ORDER BY timestamp ASC LIMIT (
                SELECT MAX(0, COUNT(*) - :keepCount) FROM search_history
            )
        )
    """)
    suspend fun deleteOldest(keepCount: Int)

    /**
     * Delete all search history.
     */
    @Query("DELETE FROM search_history")
    suspend fun deleteAll()
}
