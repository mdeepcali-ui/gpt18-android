package com.gptplus18.app.ui.screens.splash

import com.gptplus18.app.data.local.PreferencesRepository
import com.gptplus18.app.data.local.TokenStorage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SplashEntryPoint {
    fun tokenStorage(): TokenStorage
    fun prefs(): PreferencesRepository
}
