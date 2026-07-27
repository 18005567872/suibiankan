package com.suibiankan.tv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the 随便看 TV app.
 *
 * Currently stores search history only.
 */
@Database(
    entities = [SearchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao
}
