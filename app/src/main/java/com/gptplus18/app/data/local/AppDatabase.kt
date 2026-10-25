package com.gptplus18.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        private val instances = mutableMapOf<String, AppDatabase>()

        fun getDatabase(context: Context, userId: String): AppDatabase {
            return instances.getOrPut(userId) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gpt18_user_${userId}.db"
                ).build()
            }
        }
    }
}
