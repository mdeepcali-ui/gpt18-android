package com.gptplus18.app.di

import com.gptplus18.app.data.api.ApiService
import com.gptplus18.app.data.api.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module — يوفّر ApiService لكل الـ repositories
 *
 * يعتمد على RetrofitClient (Kotlin object) اللي فيه الإعدادات الكاملة.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = RetrofitClient.api
}
