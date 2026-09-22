package com.gptplus18.app.di

import android.content.Context
import androidx.room.Room
import com.gptplus18.app.data.local.room.AppDatabase
import com.gptplus18.app.data.local.room.CacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "gptplus18_cache.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideCacheDao(db: AppDatabase): CacheDao = db.cacheDao()
}
