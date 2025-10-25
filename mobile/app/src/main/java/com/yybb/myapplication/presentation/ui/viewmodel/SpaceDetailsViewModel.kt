package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.repository.SpacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SpaceDetailsViewModel @Inject constructor(
    spaceRepository: SpacesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _isColorBlindTheme = MutableStateFlow(false)
    val isColorBlindTheme: StateFlow<Boolean> = _isColorBlindTheme.asStateFlow()

    init {
        spaceRepository.isColorBlindTheme
            .onEach { _isColorBlindTheme.value = it }
            .launchIn(viewModelScope)
    }

    val spaceId: String = checkNotNull(savedStateHandle["spaceId"])
}
