package com.yybb.myapplication.data.repository

import com.yybb.myapplication.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SpacesRepository @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val isColorBlindTheme: Flow<Boolean> get() = userPreferencesRepository.isColorBlindTheme

}
