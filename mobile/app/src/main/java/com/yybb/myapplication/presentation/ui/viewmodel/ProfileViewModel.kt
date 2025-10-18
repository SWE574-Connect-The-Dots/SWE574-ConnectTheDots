package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val user: User, val isCurrentUser: Boolean) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun getProfile() {
        val userId: String? = savedStateHandle["userId"]
        repository.getProfile(userId).onEach { user ->
            // Assume we have a way to get the current user's id.
            // For now, we'll hardcode it to "1" to simulate viewing your own profile.
            val currentUserId = "1" // This should be replaced with the actual current user's ID
            _uiState.value = ProfileUiState.Success(user, userId == currentUserId)
        }.catch { e ->
            _uiState.value = ProfileUiState.Error(e.message ?: "An unknown error occurred")
        }.launchIn(viewModelScope)
    }
}
