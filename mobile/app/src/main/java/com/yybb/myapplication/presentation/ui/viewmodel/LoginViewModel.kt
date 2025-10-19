package com.yybb.myapplication.presentation.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.R
import com.yybb.myapplication.data.network.dto.LoginRequest
import com.yybb.myapplication.data.repository.AuthRepository
import com.yybb.myapplication.presentation.ui.utils.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _eventChannel = Channel<AuthEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()
    private val _viewState = MutableStateFlow<ViewState<Unit>>(ViewState.Success(Unit))
    val viewState: StateFlow<ViewState<Unit>> = _viewState


    fun onRegisterClicked() {
        viewModelScope.launch {
            _eventChannel.send(AuthEvent.NavigateToRegister)
        }
    }

    fun onLoginClicked(
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading

            when {
                username.isBlank() || password.isBlank() ->
                    _viewState.value =
                        ViewState.Error(context.getString(R.string.fill_all_fileds_error))

                else -> {
                    val loginRequest = LoginRequest(
                        username,
                        password
                    )
                    authRepository.login(loginRequest)
                        .onSuccess {
                            _viewState.value = ViewState.Success(Unit)
                            _eventChannel.send(AuthEvent.NavigateToMain)
                        }
                        .onFailure {
                            _viewState.value = ViewState.Error(it.message ?: "An unknown error occurred")
                        }
                }
            }
        }
    }

    fun clearError() {
        _viewState.value = ViewState.Success(Unit)
    }
}
