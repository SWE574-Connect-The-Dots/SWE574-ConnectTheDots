package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.Activity
import com.yybb.myapplication.data.repository.ActivityStreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ActivityStreamViewModel @Inject constructor(
    private val repository: ActivityStreamRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadActivities()
    }
    
    private fun getSinceParameter(): String {
        // Calculate 24 hours before current time
        val now = ZonedDateTime.now()
        val twentyFourHoursAgo = now.minusHours(24)
        // Format as ISO 8601 with timezone
        return twentyFourHoursAgo.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
    
    fun loadActivities(limit: Int = 100, since: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Use provided since parameter or calculate 24 hours ago
            val sinceValue = since ?: getSinceParameter()
            val result = repository.getActivityStream(limit, sinceValue)
            
            if (result.isSuccess) {
                _activities.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load activities"
            }
            
            _isLoading.value = false
        }
    }
    
    fun refresh() {
        loadActivities()
    }
    
    fun getCurrentUsername(): String? {
        return userPreferencesRepository.getCurrentUsernameSync()
    }
}
