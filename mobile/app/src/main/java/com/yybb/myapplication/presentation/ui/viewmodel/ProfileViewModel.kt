package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val user: User, val isCurrentUser: Boolean) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // We'll get the userId from navigation arguments in a real app
        getProfile("1")
    }

    private fun getProfile(userId: String) {
        repository.getProfile(userId).onEach { user ->
            // Assume we have a way to get the current user's id.
            // For now, we'll hardcode it to "1" to simulate viewing your own profile.
            val currentUserId = "1"
            _uiState.value = ProfileUiState.Success(user, userId == currentUserId)
        }.launchIn(viewModelScope)
    }
}
