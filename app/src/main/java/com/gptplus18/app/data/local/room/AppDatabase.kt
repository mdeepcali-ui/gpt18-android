package com.gptplus18.app.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SessionEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
