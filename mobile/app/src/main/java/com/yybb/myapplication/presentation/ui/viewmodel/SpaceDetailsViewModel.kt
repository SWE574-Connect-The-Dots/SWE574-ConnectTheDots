package com.yybb.myapplication.presentation.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.Discussion
import com.yybb.myapplication.data.model.SpaceDetails
import com.yybb.myapplication.data.model.toDiscussion
import com.yybb.myapplication.data.network.dto.ReportReasonItem
import com.yybb.myapplication.data.repository.ProfileRepository
import com.yybb.myapplication.data.repository.SpacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.yybb.myapplication.R
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class SpaceDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spaceRepository: SpacesRepository,
    private val profileRepository: ProfileRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _isColorBlindTheme = MutableStateFlow(false)
    val isColorBlindTheme: StateFlow<Boolean> = _isColorBlindTheme.asStateFlow()
    
    private val _spaceDetails = MutableStateFlow<SpaceDetails?>(null)
    val spaceDetails: StateFlow<SpaceDetails?> = _spaceDetails.asStateFlow()
    
    private val _discussions = MutableStateFlow<List<Discussion>>(emptyList())
    val discussions: StateFlow<List<Discussion>> = _discussions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isLoadingDiscussions = MutableStateFlow(false)
    val isLoadingDiscussions: StateFlow<Boolean> = _isLoadingDiscussions.asStateFlow()
    
    private val _isAddingDiscussion = MutableStateFlow(false)
    val isAddingDiscussion: StateFlow<Boolean> = _isAddingDiscussion.asStateFlow()
    
    private val _isJoiningLeavingSpace = MutableStateFlow(false)
    val isJoiningLeavingSpace: StateFlow<Boolean> = _isJoiningLeavingSpace.asStateFlow()
    
    private val _isDeletingSpace = MutableStateFlow(false)
    val isDeletingSpace: StateFlow<Boolean> = _isDeletingSpace.asStateFlow()
    
    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()
    
    private val _isLoadingProfile = MutableStateFlow(false)
    val isLoadingProfile: StateFlow<Boolean> = _isLoadingProfile.asStateFlow()
    
    private val _profileLoadSuccess = MutableStateFlow<String?>(null)
    val profileLoadSuccess: StateFlow<String?> = _profileLoadSuccess.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _voteRequiresCollaboratorError = MutableStateFlow(false)
    val voteRequiresCollaboratorError: StateFlow<Boolean> = _voteRequiresCollaboratorError.asStateFlow()

    private val _isLoadingReportReasons = MutableStateFlow(false)
    val isLoadingReportReasons: StateFlow<Boolean> = _isLoadingReportReasons.asStateFlow()

    private val _reportReasons = MutableStateFlow<List<ReportReasonItem>>(emptyList())
    val reportReasons: StateFlow<List<ReportReasonItem>> = _reportReasons.asStateFlow()

    private val _isSubmittingReport = MutableStateFlow(false)
    val isSubmittingReport: StateFlow<Boolean> = _isSubmittingReport.asStateFlow()

    private val _reportSubmitSuccess = MutableStateFlow<Boolean>(false)
    val reportSubmitSuccess: StateFlow<Boolean> = _reportSubmitSuccess.asStateFlow()

    private val _reportContentType = MutableStateFlow<String?>(null)
    val reportContentType: StateFlow<String?> = _reportContentType.asStateFlow()

    private val _reportContentId = MutableStateFlow<Int?>(null)
    val reportContentId: StateFlow<Int?> = _reportContentId.asStateFlow()

    fun getCurrentUsername(): String? {
        return userPreferencesRepository.getCurrentUsernameSync()
    }

    val spaceId: String = checkNotNull(savedStateHandle["spaceId"])

    init {
        spaceRepository.isColorBlindTheme
            .onEach { _isColorBlindTheme.value = it }
            .launchIn(viewModelScope)
        
        fetchSpaceDetails()
        fetchDiscussions()
    }

    private fun fetchSpaceDetails() {
        _isLoading.value = true
        _error.value = null
        
        spaceRepository.getSpaceDetails(spaceId)
            .catch { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
            .onEach { space ->
                _spaceDetails.value = space
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    private fun fetchDiscussions() {
        _isLoadingDiscussions.value = true
        
        spaceRepository.getSpaceDiscussions(spaceId)
            .catch { exception ->
                _isLoadingDiscussions.value = false
            }
            .onEach { discussions ->
                _discussions.value = discussions
                _isLoadingDiscussions.value = false
            }
            .launchIn(viewModelScope)
    }

    fun addDiscussion(text: String) {
        viewModelScope.launch {
            _isAddingDiscussion.value = true
            _error.value = null
            
            val result = spaceRepository.addDiscussion(spaceId, text)
            if (result.isSuccess) {
                fetchDiscussions()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: context.getString(R.string.failed_add_disc_message)
            }
            _isAddingDiscussion.value = false
        }
    }

    fun isUserCollaborator(): Boolean {
        val currentSpace = _spaceDetails.value
        val currentUser = userPreferencesRepository.getCurrentUsernameSync()
        return currentSpace?.collaborators?.contains(currentUser) == true
    }

    fun isUserCreator(): Boolean {
        val currentSpace = _spaceDetails.value
        val currentUser = userPreferencesRepository.getCurrentUsernameSync()
        return currentSpace?.creatorUsername == currentUser
    }

    fun joinSpace() {
        viewModelScope.launch {
            _isJoiningLeavingSpace.value = true
            _error.value = null
            
            val result = spaceRepository.joinSpace(spaceId)
            if (result.isSuccess) {
                fetchSpaceDetails()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: context.getString(R.string.failed_join_space_message)
            }
            _isJoiningLeavingSpace.value = false
        }
    }

    fun leaveSpace() {
        viewModelScope.launch {
            _isJoiningLeavingSpace.value = true
            _error.value = null
            
            val result = spaceRepository.leaveSpace(spaceId)
            if (result.isSuccess) {
                fetchSpaceDetails()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: context.getString(R.string.failed_leave_space_message)
            }
            _isJoiningLeavingSpace.value = false
        }
    }

    fun deleteSpace() {
        viewModelScope.launch {
            _isDeletingSpace.value = true
            _error.value = null
            
            val result = spaceRepository.deleteSpace(spaceId)
            if (result.isSuccess) {
                _deleteSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: context.getString(R.string.failed_delete_space_message)
            }
            _isDeletingSpace.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetDeleteSuccess() {
        _deleteSuccess.value = false
    }

    fun voteDiscussion(discussionId: String, voteType: String) {
        viewModelScope.launch {
            _error.value = null
            _voteRequiresCollaboratorError.value = false
            
            // Check if user is a collaborator before allowing vote
            if (!isUserCollaborator()) {
                _voteRequiresCollaboratorError.value = true
                return@launch
            }
            
            val currentDiscussion = _discussions.value.find { it.id.toString() == discussionId }
            if (currentDiscussion == null) {
                _error.value = context.getString(R.string.failed_vote_discussion_message)
                return@launch
            }

            val result = spaceRepository.voteDiscussion(spaceId, discussionId, voteType)
            if (result.isSuccess) {
                val updatedDiscussionDto = result.getOrNull()
                if (updatedDiscussionDto != null) {
                    val updatedDiscussion = updatedDiscussionDto.toDiscussion()
                    val updatedDiscussions = _discussions.value.map { discussion ->
                        if (discussion.id.toString() == discussionId) {
                            updatedDiscussion
                        } else {
                            discussion
                        }
                    }
                    _discussions.value = updatedDiscussions
                }
            } else {
                _error.value = result.exceptionOrNull()?.message ?: context.getString(R.string.failed_vote_discussion_message)
            }
        }
    }

    fun getProfileByUsername(username: String) {
        viewModelScope.launch {
            _isLoadingProfile.value = true
            _error.value = null
            _profileLoadSuccess.value = null
            
            try {
                val currentUsername = userPreferencesRepository.getCurrentUsernameSync()
                val usernameToFetch = if (username == currentUsername) {
                    null
                } else {
                    username
                }
                
                profileRepository.getProfile(usernameToFetch)
                    .onEach { user ->
                        _isLoadingProfile.value = false
                        _profileLoadSuccess.value = user.username
                    }
                    .catch { e ->
                        _isLoadingProfile.value = false
                        _error.value = e.message ?: context.getString(R.string.failed_get_space_det_message)
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                _isLoadingProfile.value = false
                _error.value = e.message ?: context.getString(R.string.failed_get_space_det_message)
            }
        }
    }

    fun resetProfileLoadSuccess() {
        _profileLoadSuccess.value = null
    }

    fun clearVoteRequiresCollaboratorError() {
        _voteRequiresCollaboratorError.value = false
    }

    fun fetchReportReasons(contentType: String) {
        viewModelScope.launch {
            _isLoadingReportReasons.value = true
            _error.value = null

            val result = spaceRepository.getReportReasons(contentType)
            if (result.isSuccess) {
                _reportReasons.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load report reasons"
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
            _error.value = null

            val contentType = _reportContentType.value ?: "space"
            val contentId = _reportContentId.value ?: spaceId.toIntOrNull() ?: 0

            val result = spaceRepository.submitReport(
                contentType = contentType,
                contentId = contentId,
                reason = reason
            )
            if (result.isSuccess) {
                _reportSubmitSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to submit report"
            }
            _isSubmittingReport.value = false
        }
    }

    fun resetReportSubmitSuccess() {
        _reportSubmitSuccess.value = false
    }
}
