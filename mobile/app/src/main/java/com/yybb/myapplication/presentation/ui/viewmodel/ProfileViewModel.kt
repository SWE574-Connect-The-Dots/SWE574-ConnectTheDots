package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun getProfile() {
        val username: String? = savedStateHandle["username"]
        viewModelScope.launch {
            val currentUsername = userPreferencesRepository.username.first()
            val userIdToFetch = if (username == null || username == currentUsername) {
                null
            } else {
                username
            }
            
            repository.getProfile(userIdToFetch).onEach { user ->
                val isCurrentUser = username == null || username == currentUsername || user.username == currentUsername
                _uiState.value = ProfileUiState.Success(user, isCurrentUser)
            }.catch { e ->
                _uiState.value = ProfileUiState.Error(e.message ?: "An unknown error occurred")
            }.launchIn(viewModelScope)
        }
    }
}
