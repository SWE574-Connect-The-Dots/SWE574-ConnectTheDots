package com.yybb.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val COLOR_BLIND_THEME = booleanPreferencesKey("color_blind_theme")
        val USERNAME = stringPreferencesKey("username")
    }

    val isColorBlindTheme: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.COLOR_BLIND_THEME] ?: false
        }

    val username: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USERNAME]
        }

    suspend fun setColorBlindTheme(isColorBlind: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_BLIND_THEME] = isColorBlind
        }
    }

    suspend fun setUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
        }
    }

    suspend fun clearUsername() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USERNAME)
        }
    }

    fun getCurrentUsernameSync(): String? {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.USERNAME]
            }.first()
        }
    }
}
