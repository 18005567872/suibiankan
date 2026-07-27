package com.suibiankan.tv.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing search history queries.
 */
@Entity(
    tableName = "search_history",
    indices = [
        Index(value = ["query"], unique = true)
    ]
)
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "query")
    val query: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
