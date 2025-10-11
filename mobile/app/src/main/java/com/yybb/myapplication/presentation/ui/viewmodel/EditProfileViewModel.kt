package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EditProfileUiState {
    object Loading : EditProfileUiState
    data class Success(val user: User) : EditProfileUiState
    data class Error(val message: String) : EditProfileUiState
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        // TODO Update when backend is deployed
        getProfile("1")
    }

    private fun getProfile(userId: String) {
        repository.getProfile(userId).onEach { user ->
            _uiState.value = EditProfileUiState.Success(user)
        }.launchIn(viewModelScope)
    }


    fun saveProfile(profession: String, bio: String) {
        viewModelScope.launch {
            // repository.updateProfile("1", profession, bio)
        }
    }
}
