package com.yybb.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val COLOR_BLIND_THEME = booleanPreferencesKey("color_blind_theme")
    }

    val isColorBlindTheme: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.COLOR_BLIND_THEME] ?: false
        }

    suspend fun setColorBlindTheme(isColorBlind: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_BLIND_THEME] = isColorBlind
        }
    }
}
