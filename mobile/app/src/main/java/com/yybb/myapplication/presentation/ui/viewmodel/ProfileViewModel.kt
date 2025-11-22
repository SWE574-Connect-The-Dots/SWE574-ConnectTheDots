package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.network.dto.ReportReasonItem
import com.yybb.myapplication.data.repository.ProfileRepository
import com.yybb.myapplication.data.repository.SpacesRepository
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
    private val savedStateHandle: SavedStateHandle,
    private val spacesRepository: SpacesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Report-related state
    private val _isLoadingReportReasons = MutableStateFlow(false)
    val isLoadingReportReasons: StateFlow<Boolean> = _isLoadingReportReasons.asStateFlow()

    private val _reportReasons = MutableStateFlow<List<ReportReasonItem>>(emptyList())
    val reportReasons: StateFlow<List<ReportReasonItem>> = _reportReasons.asStateFlow()

    private val _isSubmittingReport = MutableStateFlow(false)
    val isSubmittingReport: StateFlow<Boolean> = _isSubmittingReport.asStateFlow()

    private val _reportSubmitSuccess = MutableStateFlow(false)
    val reportSubmitSuccess: StateFlow<Boolean> = _reportSubmitSuccess.asStateFlow()

    private val _reportError = MutableStateFlow<String?>(null)
    val reportError: StateFlow<String?> = _reportError.asStateFlow()

    private val _reportContentType = MutableStateFlow<String?>(null)
    private val _reportContentId = MutableStateFlow<Int?>(null)

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

    fun fetchReportReasons(contentType: String) {
        viewModelScope.launch {
            _isLoadingReportReasons.value = true
            _reportError.value = null

            val result = spacesRepository.getReportReasons(contentType)
            if (result.isSuccess) {
                _reportReasons.value = result.getOrNull() ?: emptyList()
            } else {
                _reportError.value = result.exceptionOrNull()?.message ?: "Failed to load report reasons"
            }
            _isLoadingReportReasons.value = false
        }
    }

    fun prepareReport(contentType: String, contentId: Int) {
        _reportContentType.value = contentType
        _reportContentId.value = contentId
        fetchReportReasons(contentType)
    }

    fun submitReport(reason: String) {
        viewModelScope.launch {
            _isSubmittingReport.value = true
            _reportError.value = null

            val contentType = _reportContentType.value ?: "profile"
            val contentId = _reportContentId.value ?: 0

            val result = spacesRepository.submitReport(
                contentType = contentType,
                contentId = contentId,
                reason = reason
            )
            if (result.isSuccess) {
                _reportSubmitSuccess.value = true
            } else {
                _reportError.value = result.exceptionOrNull()?.message ?: "Failed to submit report"
            }
            _isSubmittingReport.value = false
        }
    }

    fun resetReportSubmitSuccess() {
        _reportSubmitSuccess.value = false
    }

    fun clearReportError() {
        _reportError.value = null
    }
}
