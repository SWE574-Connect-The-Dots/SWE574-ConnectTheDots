package com.yybb.myapplication.data.repository

import com.yybb.myapplication.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val isColorBlindTheme: Flow<Boolean> get() = userPreferencesRepository.isColorBlindTheme

    suspend fun setColorBlindTheme(isColorBlind: Boolean) {
        userPreferencesRepository.setColorBlindTheme(isColorBlind)
    }
}
