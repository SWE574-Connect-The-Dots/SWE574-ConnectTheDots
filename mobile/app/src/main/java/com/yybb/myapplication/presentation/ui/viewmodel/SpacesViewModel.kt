package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.SpaceDetails
import com.yybb.myapplication.data.model.SpaceTag
import com.yybb.myapplication.data.network.dto.SpaceDetailsResponse
import com.yybb.myapplication.data.repository.SpacesRepository
import com.yybb.myapplication.presentation.ui.utils.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class SpacesSection {
    TRENDING,
    NEW
}

data class SpaceListItem(
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: String,
    val creatorUsername: String,
    val collaboratorsCount: Int,
    val tags: List<SpaceTag>,
    val isJoined: Boolean,
    val isArchived: Boolean = false
) {
    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(createdAt)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            createdAt
        }
    }
    
    fun getTruncatedDescription(maxLength: Int = 200): String {
        return if (description.length > maxLength) {
            description.take(maxLength) + "..."
        } else {
            description
        }
    }
}

@HiltViewModel
class SpacesViewModel @Inject constructor(
    private val repository: SpacesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _eventFlow = Channel<SpacesEvent>()
    val eventFlow = _eventFlow.receiveAsFlow()

    private val _selectedSection = MutableStateFlow(SpacesSection.TRENDING)
    val selectedSection: StateFlow<SpacesSection> = _selectedSection.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allSpaces = MutableStateFlow<List<SpaceListItem>>(emptyList())
    val allSpaces: StateFlow<List<SpaceListItem>> = _allSpaces.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTrendingSpaces()
    }

    val filteredSpaces: StateFlow<List<SpaceListItem>> = combine(
        allSpaces,
        searchQuery
    ) { spaces, query ->
        var filtered = spaces
        
        // Filter by search query
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filtered = filtered.filter { space ->
                space.title.lowercase().contains(lowerQuery) ||
                space.description.lowercase().contains(lowerQuery) ||
                space.creatorUsername.lowercase().contains(lowerQuery) ||
                space.tags.any { tag -> tag.name.lowercase().contains(lowerQuery) }
            }
        }
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onGoToDetailsClicked(spaceId: Int) {
        viewModelScope.launch {
            _eventFlow.send(SpacesEvent.NavigateToSpaceDetails(spaceId))
        }
    }

    fun selectSection(section: SpacesSection) {
        _selectedSection.value = section
        when (section) {
            SpacesSection.TRENDING -> loadTrendingSpaces()
            SpacesSection.NEW -> loadNewSpaces()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun loadTrendingSpaces() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getTrendingSpaces()
                .onSuccess { spaces ->
                    val currentUsername = userPreferencesRepository.username.first()
                    _allSpaces.value = spaces.map { it.toSpaceListItem(currentUsername) }
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
        }
    }

    private fun loadNewSpaces() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getNewSpaces()
                .onSuccess { spaces ->
                    val currentUsername = userPreferencesRepository.username.first()
                    _allSpaces.value = spaces.map { it.toSpaceListItem(currentUsername) }
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
        }
    }

    fun onJoinSpaceClick(spaceId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.joinSpace(spaceId.toString())
                .onSuccess {
                    val refreshResult = when (_selectedSection.value) {
                        SpacesSection.TRENDING -> {
                            repository.getTrendingSpaces()
                        }
                        SpacesSection.NEW -> {
                            repository.getNewSpaces()
                        }
                    }
                    
                    refreshResult.onSuccess { spaces ->
                        val currentUsername = userPreferencesRepository.username.first()
                        _allSpaces.value = spaces.map { it.toSpaceListItem(currentUsername) }
                        _isLoading.value = false
                        _eventFlow.send(SpacesEvent.NavigateToSpaceDetails(spaceId))
                    }.onFailure { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
        }
    }

    fun onLeaveSpaceClick(spaceId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.leaveSpace(spaceId.toString())
                .onSuccess {
                    _isLoading.value = false
                    when (_selectedSection.value) {
                        SpacesSection.TRENDING -> loadTrendingSpaces()
                        SpacesSection.NEW -> loadNewSpaces()
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun SpaceDetailsResponse.toSpaceListItem(currentUsername: String?): SpaceListItem {
        val isJoined = currentUsername != null && collaborators.contains(currentUsername)
        return SpaceListItem(
            id = this.id,
            title = this.title,
            description = this.description,
            createdAt = this.createdAt,
            creatorUsername = this.creatorUsername,
            collaboratorsCount = this.collaborators.size,
            tags = this.tags.map { 
                SpaceTag(
                    id = it.id,
                    name = it.name,
                    wikidataId = it.wikidataId,
                    wikidataLabel = it.wikidataLabel
                )
            },
            isJoined = isJoined,
            isArchived = this.isArchived
        )
    }
}
