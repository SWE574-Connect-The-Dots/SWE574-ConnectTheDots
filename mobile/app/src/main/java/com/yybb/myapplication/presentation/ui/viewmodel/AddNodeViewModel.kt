package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.network.dto.AddNodeLocation
import com.yybb.myapplication.data.network.dto.AddNodeProperty
import com.yybb.myapplication.data.network.dto.AddNodePropertyValue
import com.yybb.myapplication.data.network.dto.AddNodeRequest
import com.yybb.myapplication.data.network.dto.AddNodeResponse
import com.yybb.myapplication.data.network.dto.AddNodeWikidataEntity
import com.yybb.myapplication.data.repository.SpaceNodeDetailsRepository
import com.yybb.myapplication.data.repository.SpaceNodesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNodeViewModel @Inject constructor(
    private val spaceNodeDetailsRepository: SpaceNodeDetailsRepository,
    private val spaceNodesRepository: SpaceNodesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spaceId: String = checkNotNull(savedStateHandle["spaceId"])

    // Wikidata entity search
    private val _wikidataSearchResults = MutableStateFlow<List<WikidataProperty>>(emptyList())
    val wikidataSearchResults: StateFlow<List<WikidataProperty>> = _wikidataSearchResults.asStateFlow()

    private val _isSearchingWikidata = MutableStateFlow(false)
    val isSearchingWikidata: StateFlow<Boolean> = _isSearchingWikidata.asStateFlow()

    private val _wikidataSearchError = MutableStateFlow<String?>(null)
    val wikidataSearchError: StateFlow<String?> = _wikidataSearchError.asStateFlow()

    // Selected entity
    private val _selectedEntity = MutableStateFlow<WikidataProperty?>(null)
    val selectedEntity: StateFlow<WikidataProperty?> = _selectedEntity.asStateFlow()

    // Entity properties
    private val _entityProperties = MutableStateFlow<List<NodeProperty>>(emptyList())
    val entityProperties: StateFlow<List<NodeProperty>> = _entityProperties.asStateFlow()

    private val _isLoadingProperties = MutableStateFlow(false)
    val isLoadingProperties: StateFlow<Boolean> = _isLoadingProperties.asStateFlow()

    private val _propertiesError = MutableStateFlow<String?>(null)
    val propertiesError: StateFlow<String?> = _propertiesError.asStateFlow()

    // Property search and selection
    private val _propertySearchQuery = MutableStateFlow("")
    val propertySearchQuery: StateFlow<String> = _propertySearchQuery.asStateFlow()

    private val _selectedProperties = MutableStateFlow<Set<String>>(emptySet())
    val selectedProperties: StateFlow<Set<String>> = _selectedProperties.asStateFlow()

    private val _filteredProperties = MutableStateFlow<List<NodeProperty>>(emptyList())
    val filteredProperties: StateFlow<List<NodeProperty>> = _filteredProperties.asStateFlow()

    // Space nodes for connection
    private val _availableNodes = MutableStateFlow<List<SpaceNode>>(emptyList())
    val availableNodes: StateFlow<List<SpaceNode>> = _availableNodes.asStateFlow()

    private val _isLoadingNodes = MutableStateFlow(false)
    val isLoadingNodes: StateFlow<Boolean> = _isLoadingNodes.asStateFlow()

    // Edge label search
    private var edgeLabelSearchJob: Job? = null
    private val _edgeLabelSearchResults = MutableStateFlow<List<WikidataProperty>>(emptyList())
    val edgeLabelSearchResults: StateFlow<List<WikidataProperty>> = _edgeLabelSearchResults.asStateFlow()

    private val _isEdgeLabelSearching = MutableStateFlow(false)
    val isEdgeLabelSearching: StateFlow<Boolean> = _isEdgeLabelSearching.asStateFlow()

    private val _edgeLabelSearchError = MutableStateFlow<String?>(null)
    val edgeLabelSearchError: StateFlow<String?> = _edgeLabelSearchError.asStateFlow()

    private val _isCreatingNode = MutableStateFlow(false)
    val isCreatingNode: StateFlow<Boolean> = _isCreatingNode.asStateFlow()

    private val _createNodeError = MutableStateFlow<String?>(null)
    val createNodeError: StateFlow<String?> = _createNodeError.asStateFlow()

    private val _createNodeSuccess = MutableStateFlow<String?>(null)
    val createNodeSuccess: StateFlow<String?> = _createNodeSuccess.asStateFlow()

    // Navigation events
    private val _eventChannel = Channel<AddNodeEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    init {
        loadSpaceNodes()
        observePropertyFilter()
    }

    fun searchWikidataEntities(query: String) {
        viewModelScope.launch {
            _isSearchingWikidata.value = true
            _wikidataSearchError.value = null

            val result = spaceNodeDetailsRepository.searchWikidataEntities(query)
            result.onSuccess { properties ->
                _wikidataSearchResults.value = properties
            }.onFailure { throwable ->
                _wikidataSearchResults.value = emptyList()
                _wikidataSearchError.value = throwable.message
            }

            _isSearchingWikidata.value = false
        }
    }

    fun selectEntity(entity: WikidataProperty) {
        _selectedEntity.value = entity
        _wikidataSearchResults.value = emptyList()
        loadEntityProperties(entity.id)
    }

    private fun loadEntityProperties(entityId: String) {
        viewModelScope.launch {
            _isLoadingProperties.value = true
            _propertiesError.value = null
            _selectedProperties.value = emptySet()

            val result = spaceNodeDetailsRepository.getWikidataEntityProperties(entityId)
            result.onSuccess { properties ->
                _entityProperties.value = properties
            }.onFailure { throwable ->
                _entityProperties.value = emptyList()
                _propertiesError.value = throwable.message
            }

            _isLoadingProperties.value = false
        }
    }

    fun updatePropertySearchQuery(query: String) {
        _propertySearchQuery.value = query
    }

    fun togglePropertySelection(statementId: String) {
        _selectedProperties.value = if (_selectedProperties.value.contains(statementId)) {
            _selectedProperties.value - statementId
        } else {
            _selectedProperties.value + statementId
        }
    }

    private fun observePropertyFilter() {
        viewModelScope.launch {
            combine(_propertySearchQuery, _entityProperties) { query, properties ->
                if (query.length > 2) {
                    properties.filter {
                        it.propertyLabel.contains(query, ignoreCase = true) ||
                        it.valueText.contains(query, ignoreCase = true)
                    }
                } else {
                    // Show all properties when query is empty or less than 3 characters
                    properties
                }
            }.collect { filtered ->
                _filteredProperties.value = filtered
            }
        }
    }

    private fun loadSpaceNodes() {
        viewModelScope.launch {
            _isLoadingNodes.value = true
            val result = spaceNodesRepository.getSpaceNodes(spaceId)
            result.onSuccess { nodes ->
                _availableNodes.value = nodes.sortedBy { it.label.lowercase() }
            }.onFailure {
                _availableNodes.value = emptyList()
            }
            _isLoadingNodes.value = false
        }
    }

    fun searchEdgeLabelOptions(query: String) {
        val normalized = query.trim()
        edgeLabelSearchJob?.cancel()

        if (normalized.length <= 2) {
            _isEdgeLabelSearching.value = false
            _edgeLabelSearchResults.value = emptyList()
            _edgeLabelSearchError.value = null
            return
        }

        edgeLabelSearchJob = viewModelScope.launch {
            _isEdgeLabelSearching.value = true
            _edgeLabelSearchError.value = null

            val result = spaceNodeDetailsRepository.searchWikidataProperties(normalized)
            result.onSuccess { properties ->
                _edgeLabelSearchResults.value = properties
            }.onFailure { throwable ->
                _edgeLabelSearchResults.value = emptyList()
                _edgeLabelSearchError.value = throwable.message
            }

            _isEdgeLabelSearching.value = false
        }
    }

    fun resetEdgeLabelSearch() {
        edgeLabelSearchJob?.cancel()
        _isEdgeLabelSearching.value = false
        _edgeLabelSearchResults.value = emptyList()
        _edgeLabelSearchError.value = null
    }

    fun clearWikidataSearchError() {
        _wikidataSearchError.value = null
    }

    fun createNode(
        relatedNodeId: String?,
        edgeLabel: String,
        isNewNodeSource: Boolean,
        selectedEdgeLabelProperty: WikidataProperty?
    ) {
        val selectedEntity = _selectedEntity.value
        if (selectedEntity == null) {
            _createNodeError.value = "Please select an entity"
            return
        }

        if (edgeLabel.isBlank()) {
            _createNodeError.value = "Please enter an edge label"
            return
        }

        // Build selected properties
        val selectedPropertyStatementIds = _selectedProperties.value
        val selectedPropertiesList = _entityProperties.value
            .filter { it.statementId in selectedPropertyStatementIds }
            .map { property ->
                AddNodeProperty(
                    statementId = property.statementId,
                    property = property.propertyId,
                    display = property.display,
                    propertyLabel = property.propertyLabel,
                    value = AddNodePropertyValue(
                        type = if (property.isEntity) "entity" else "string",
                        id = property.entityId,
                        text = property.valueText
                    ),
                    wikidataEntity = AddNodeWikidataEntity(
                        id = selectedEntity.id,
                        label = selectedEntity.label,
                        description = selectedEntity.description,
                        url = selectedEntity.url,
                        wikidataPropertyId = selectedEdgeLabelProperty?.id
                    )
                )
            }

        val request = AddNodeRequest(
            relatedNodeId = relatedNodeId,
            wikidataEntity = AddNodeWikidataEntity(
                id = selectedEntity.id,
                label = selectedEntity.label,
                description = selectedEntity.description,
                url = selectedEntity.url,
                wikidataPropertyId = selectedEdgeLabelProperty?.id
            ),
            edgeLabel = edgeLabel.trim(),
            isNewNodeSource = isNewNodeSource,
            location = AddNodeLocation(), // Empty location for now
            selectedProperties = selectedPropertiesList
        )

        viewModelScope.launch {
            _isCreatingNode.value = true
            _createNodeError.value = null
            _createNodeSuccess.value = null

            val result = spaceNodeDetailsRepository.addNode(spaceId, request)
            result.onSuccess { response ->
                // After successful node creation, create a snapshot
                val snapshotResult = spaceNodeDetailsRepository.createSnapshot(spaceId)
                snapshotResult.onSuccess {
                    _createNodeSuccess.value = response.message
                    _isCreatingNode.value = false
                    // Emit navigation event
                    _eventChannel.send(AddNodeEvent.NavigateBack)
                }.onFailure { throwable ->
                    // If snapshot creation fails, still show success but log the error
                    _createNodeSuccess.value = response.message
                    _isCreatingNode.value = false
                    // Emit navigation event even if snapshot fails
                    _eventChannel.send(AddNodeEvent.NavigateBack)
                }
            }.onFailure { throwable ->
                _createNodeError.value = throwable.message ?: "Failed to create node"
                _isCreatingNode.value = false
            }
        }
    }

    fun clearCreateNodeError() {
        _createNodeError.value = null
    }

    fun clearCreateNodeSuccess() {
        _createNodeSuccess.value = null
    }

    override fun onCleared() {
        super.onCleared()
        edgeLabelSearchJob?.cancel()
    }
}

