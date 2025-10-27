package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.repository.SpacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


sealed interface CreateSpaceUiState {
    object Loading : CreateSpaceUiState
    data class Success(val user: User) : CreateSpaceUiState
    data class Error(val message: String) : CreateSpaceUiState
}

@HiltViewModel
class CreateSpaceViewModel@Inject constructor(
    spaceRepository: SpacesRepository,
) : ViewModel()
{

    private val _uiState = MutableStateFlow<CreateSpaceUiState>(CreateSpaceUiState.Loading)
    val uiState: StateFlow<CreateSpaceUiState> = _uiState.asStateFlow()

    private val _isColorBlindTheme = MutableStateFlow(false)
    val isColorBlindTheme: StateFlow<Boolean> = _isColorBlindTheme.asStateFlow()

    init {
        spaceRepository.isColorBlindTheme
            .onEach { _isColorBlindTheme.value = it }
            .launchIn(viewModelScope)
    }

}