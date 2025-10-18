package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.repository.AuthRepository
import com.yybb.myapplication.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _isColorBlindTheme = MutableStateFlow(false)
    val isColorBlindTheme: StateFlow<Boolean> = _isColorBlindTheme.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    init {
        settingsRepository.isColorBlindTheme
            .onEach { _isColorBlindTheme.value = it }
            .launchIn(viewModelScope)
    }

    fun setColorBlindTheme(isColorBlind: Boolean) {
        viewModelScope.launch {
            settingsRepository.setColorBlindTheme(isColorBlind)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutEvent.emit(Unit)
        }
    }
}
