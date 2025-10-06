package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.repository.SpacesRepository
import com.yybb.myapplication.presentation.ui.utils.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpacesViewModel @Inject constructor(
    private val repository: SpacesRepository
) : ViewModel() {

    private val _eventFlow = Channel<SpacesEvent>()
    val eventFlow = _eventFlow.receiveAsFlow()

    private val _uiState = MutableStateFlow<ViewState<String>>(ViewState.Success("Initial Data"))
    val uiState = _uiState.asStateFlow()

    fun onGoToDetailsClicked(spaceId: Int) {
        viewModelScope.launch {
            _eventFlow.send(SpacesEvent.NavigateToSpaceDetails(spaceId))
        }
    }

    fun fetchData(isSuccess: Boolean) {
        viewModelScope.launch {
            _uiState.value = ViewState.Loading
            delay(1000)
            _uiState.value = if (isSuccess) {
                ViewState.Success("This is a successful result!")
            } else {
                ViewState.Error("This is a fake error message.")
            }
        }
    }
}
