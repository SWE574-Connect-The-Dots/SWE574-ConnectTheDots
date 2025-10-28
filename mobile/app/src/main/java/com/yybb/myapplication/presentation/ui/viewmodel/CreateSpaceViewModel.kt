package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.TagDto
import com.yybb.myapplication.data.repository.SpacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreateSpaceUiState {
    object Loading : CreateSpaceUiState
    data class Success(val tags: List<TagDto>, val showResults: Boolean = false) : CreateSpaceUiState
    data class Error(val message: String) : CreateSpaceUiState
    object Initial : CreateSpaceUiState
}

@Stable
data class CreateSpaceFormState(
    val spaceTitle: String = "",
    val spaceDescription: String = "",
    val selectedTags: List<TagDto> = emptyList(),
    val showTagSearch: Boolean = false,
    var tagSearchQuery: String = "",
    var showSearchResults: Boolean = false
) {
    val isFormValid: Boolean
        get() = spaceTitle.isNotBlank() && spaceDescription.isNotBlank()

    val isSearchEnabled: Boolean
        get() = tagSearchQuery.isNotBlank()
}

@HiltViewModel
class CreateSpaceViewModel @Inject constructor(
    private val apiService: ApiService,
    private val spaceRepository: SpacesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateSpaceUiState>(CreateSpaceUiState.Initial)
    val uiState: StateFlow<CreateSpaceUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CreateSpaceFormState())
    val formState: StateFlow<CreateSpaceFormState> = _formState.asStateFlow()

    private val _isColorBlindTheme = MutableStateFlow(false)
    val isColorBlindTheme: StateFlow<Boolean> = _isColorBlindTheme.asStateFlow()

    init {
        spaceRepository.isColorBlindTheme
            .onEach { _isColorBlindTheme.value = it }
            .launchIn(viewModelScope)
    }

    fun getTags(tagQuery: String) {
        viewModelScope.launch {
            _uiState.value = CreateSpaceUiState.Loading
            spaceRepository.getWikiTags(tagQuery)
                .onSuccess {
                    _uiState.value = CreateSpaceUiState.Success(it, showResults = true)  // ✅ Show results
                }
                .onFailure {
                    _uiState.value = CreateSpaceUiState.Error(it.message ?: "An unknown error occurred")
                }
        }
    }

    fun updateFormState(newState: CreateSpaceFormState) {
        if (newState.showTagSearch != _formState.value.showTagSearch) {
            if (newState.showTagSearch == false) {
                newState.showSearchResults = false
                newState.tagSearchQuery = ""
            }
        }
        _formState.value = newState
    }

    fun resetFormState() {
        _formState.value = CreateSpaceFormState()
    }

    fun resetState() {
        _uiState.value = CreateSpaceUiState.Initial
    }


}