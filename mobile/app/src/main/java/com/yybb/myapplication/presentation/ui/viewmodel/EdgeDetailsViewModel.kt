package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.repository.SpaceNodeDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EdgeDetailsViewModel @Inject constructor(
    private val spaceNodeDetailsRepository: SpaceNodeDetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spaceId: String = checkNotNull(savedStateHandle["spaceId"])
    private val edgeId: String = checkNotNull(savedStateHandle["edgeId"])
    private val edgeLabelArg: String? = savedStateHandle["edgeLabel"]
    private val sourceId: String = checkNotNull(savedStateHandle["sourceId"])
    private val sourceName: String = checkNotNull(savedStateHandle["sourceName"])
    private val targetId: String = checkNotNull(savedStateHandle["targetId"])
    private val targetName: String = checkNotNull(savedStateHandle["targetName"])

    private val _edgeLabel = MutableStateFlow(edgeLabelArg ?: "")
    val edgeLabel: StateFlow<String> = _edgeLabel.asStateFlow()

    private val _selectedProperty = MutableStateFlow<WikidataProperty?>(null)
    val selectedProperty: StateFlow<WikidataProperty?> = _selectedProperty.asStateFlow()

    private val _edgeLabelSearchResults = MutableStateFlow<List<WikidataProperty>>(emptyList())
    val edgeLabelSearchResults: StateFlow<List<WikidataProperty>> = _edgeLabelSearchResults.asStateFlow()

    private val _isEdgeLabelSearching = MutableStateFlow(false)
    val isEdgeLabelSearching: StateFlow<Boolean> = _isEdgeLabelSearching.asStateFlow()

    private val _edgeLabelSearchError = MutableStateFlow<String?>(null)
    val edgeLabelSearchError: StateFlow<String?> = _edgeLabelSearchError.asStateFlow()

    private val _isUpdatingEdge = MutableStateFlow(false)
    val isUpdatingEdge: StateFlow<Boolean> = _isUpdatingEdge.asStateFlow()

    private val _updateEdgeError = MutableStateFlow<String?>(null)
    val updateEdgeError: StateFlow<String?> = _updateEdgeError.asStateFlow()

    private val _updateEdgeSuccess = MutableStateFlow<Boolean>(false)
    val updateEdgeSuccess: StateFlow<Boolean> = _updateEdgeSuccess.asStateFlow()

    private val _isDeletingEdge = MutableStateFlow(false)
    val isDeletingEdge: StateFlow<Boolean> = _isDeletingEdge.asStateFlow()

    private val _deleteEdgeError = MutableStateFlow<String?>(null)
    val deleteEdgeError: StateFlow<String?> = _deleteEdgeError.asStateFlow()

    private val _deleteEdgeSuccess = MutableStateFlow<Boolean>(false)
    val deleteEdgeSuccess: StateFlow<Boolean> = _deleteEdgeSuccess.asStateFlow()

    private val _spaceNodes = MutableStateFlow<List<SpaceNode>>(emptyList())
    val spaceNodes: StateFlow<List<SpaceNode>> = _spaceNodes.asStateFlow()

    private val _isLoadingNodes = MutableStateFlow(false)
    val isLoadingNodes: StateFlow<Boolean> = _isLoadingNodes.asStateFlow()

    private val _nodesLoadError = MutableStateFlow<String?>(null)
    val nodesLoadError: StateFlow<String?> = _nodesLoadError.asStateFlow()

    private var edgeLabelSearchJob: Job? = null

    init {
        // Initial search with edge label when entering the screen
        val initialLabel = edgeLabelArg ?: ""
        if (initialLabel.isNotBlank()) {
            searchEdgeLabelOptions(initialLabel, isInitialLoad = true)
        }
        // Fetch nodes to get source and target node details
        fetchSpaceNodes()
    }

    private fun fetchSpaceNodes() {
        viewModelScope.launch {
            _isLoadingNodes.value = true
            _nodesLoadError.value = null

            val result = spaceNodeDetailsRepository.getSpaceNodes(spaceId)
            result.onSuccess { nodes ->
                _spaceNodes.value = nodes
            }.onFailure { throwable ->
                _nodesLoadError.value = throwable.message
                _spaceNodes.value = emptyList()
            }

            _isLoadingNodes.value = false
        }
    }

    fun getSourceNodeDisplayText(): String {
        val node = _spaceNodes.value.find { it.id.toString() == sourceId }
        return if (node != null) {
            val wikidataPart = node.wikidataId?.let { " ($it)" } ?: ""
            "${node.label}$wikidataPart"
        } else {
            // Fallback to sourceName if node not found
            "$sourceName ($sourceId)"
        }
    }

    fun getTargetNodeDisplayText(): String {
        val node = _spaceNodes.value.find { it.id.toString() == targetId }
        return if (node != null) {
            val wikidataPart = node.wikidataId?.let { " ($it)" } ?: ""
            "${node.label}$wikidataPart"
        } else {
            // Fallback to targetName if node not found
            "$targetName ($targetId)"
        }
    }

    fun searchEdgeLabelOptions(query: String, isInitialLoad: Boolean = false) {
        val normalized = query.trim()
        edgeLabelSearchJob?.cancel()

        if (normalized.length < 3 && !isInitialLoad) {
            _isEdgeLabelSearching.value = false
            _edgeLabelSearchResults.value = emptyList()
            _edgeLabelSearchError.value = null
            return
        }

        edgeLabelSearchJob = viewModelScope.launch {
            _isEdgeLabelSearching.value = true
            _edgeLabelSearchError.value = null

            val result = spaceNodeDetailsRepository.searchWikidataEdgeLabels(normalized)
            result.onSuccess { properties ->
                _edgeLabelSearchResults.value = properties
            }.onFailure { throwable ->
                _edgeLabelSearchResults.value = emptyList()
                _edgeLabelSearchError.value = throwable.message
            }

            _isEdgeLabelSearching.value = false
        }
    }

    fun selectProperty(property: WikidataProperty) {
        _selectedProperty.value = property
        _edgeLabel.value = property.label
        resetEdgeLabelSearch()
    }

    fun updateEdgeLabel(label: String) {
        _edgeLabel.value = label
        _selectedProperty.value = null
    }

    fun resetEdgeLabelSearch() {
        edgeLabelSearchJob?.cancel()
        _isEdgeLabelSearching.value = false
        _edgeLabelSearchResults.value = emptyList()
        _edgeLabelSearchError.value = null
    }

    fun clearEdgeLabelSearchError() {
        _edgeLabelSearchError.value = null
    }

    fun updateEdge(
        label: String,
        isForwardDirection: Boolean
    ) {
        viewModelScope.launch {
            _isUpdatingEdge.value = true
            _updateEdgeError.value = null
            _updateEdgeSuccess.value = false

            // Determine source and target based on direction
            val finalSourceId = if (isForwardDirection) sourceId else targetId
            val finalTargetId = if (isForwardDirection) targetId else sourceId

            // Get wikidata property ID from selected property, or use empty string
            val wikidataPropertyId = _selectedProperty.value?.id ?: ""

            val result = spaceNodeDetailsRepository.updateEdgeDetails(
                spaceId = spaceId,
                edgeId = edgeId,
                label = label,
                sourceId = finalSourceId,
                targetId = finalTargetId,
                wikidataPropertyId = wikidataPropertyId
            )

            result.onSuccess {
                _updateEdgeSuccess.value = true
                // Update the edge label in state
                _edgeLabel.value = label
            }.onFailure { throwable ->
                _updateEdgeError.value = throwable.message
            }

            _isUpdatingEdge.value = false
        }
    }

    fun clearUpdateEdgeError() {
        _updateEdgeError.value = null
    }

    fun resetUpdateEdgeSuccess() {
        _updateEdgeSuccess.value = false
    }

    fun deleteEdge() {
        viewModelScope.launch {
            _isDeletingEdge.value = true
            _deleteEdgeError.value = null
            _deleteEdgeSuccess.value = false

            val result = spaceNodeDetailsRepository.deleteEdge(
                spaceId = spaceId,
                edgeId = edgeId
            )

            result.onSuccess {
                _deleteEdgeSuccess.value = true
            }.onFailure { throwable ->
                _deleteEdgeError.value = throwable.message
            }

            _isDeletingEdge.value = false
        }
    }

    fun clearDeleteEdgeError() {
        _deleteEdgeError.value = null
    }

    fun resetDeleteEdgeSuccess() {
        _deleteEdgeSuccess.value = false
    }

    val spaceIdValue: String get() = spaceId
    val edgeIdValue: String get() = edgeId
    val sourceIdValue: String get() = sourceId
    val sourceNameValue: String get() = sourceName
    val targetIdValue: String get() = targetId
    val targetNameValue: String get() = targetName
}

