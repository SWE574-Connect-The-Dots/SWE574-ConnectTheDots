package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _eventChannel = Channel<AuthEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    fun onLoginClicked() {
        viewModelScope.launch {
            _eventChannel.send(AuthEvent.NavigateToMain)
        }
    }
}
