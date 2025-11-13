package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.repository.SpaceNodeDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SpaceNodeDetailsViewModel @Inject constructor(
    private val spaceNodeDetailsRepository: SpaceNodeDetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class NodeDetailsMock(
        val name: String,
        val wikidataId: String,
        val availableProperties: List<String>,
        val defaultProperties: List<String>
    )

    data class PropertyOption(
        val property: NodeProperty,
        val isChecked: Boolean
    )

    data class NodeOption(
        val id: String,
        val name: String
    )

    data class NodeConnection(
        val edgeId: String,
        val label: String,
        val isSource: Boolean
    )

    data class EdgeCreationResult(
        val edgeId: Int,
        val snapshotId: Int,
        val isForwardDirection: Boolean,
        val connectedNodeName: String,
        val label: String
    )

    private val spaceId: String = checkNotNull(savedStateHandle["spaceId"])
    private val nodeId: String = checkNotNull(savedStateHandle["nodeId"])
    private val nodeLabelArg: String? = savedStateHandle["nodeLabel"]
    private val nodeWikidataIdArg: String? = savedStateHandle["nodeWikidataId"]

    private val nodeDetails: NodeDetailsMock = nodeDetailsMap[nodeId] ?: nodeDetailsMap.values.first()

    private val _nodeName = MutableStateFlow(nodeLabelArg ?: nodeDetails.name)
    val nodeName: StateFlow<String> = _nodeName.asStateFlow()

    private val _wikidataId = MutableStateFlow(
        nodeWikidataIdArg?.takeUnless { it.isBlank() } ?: nodeDetails.wikidataId
    )
    val wikidataId: StateFlow<String> = _wikidataId.asStateFlow()

    private val _availableConnectionNodes = MutableStateFlow<List<NodeOption>>(emptyList())
    val availableConnectionNodes: StateFlow<List<NodeOption>> = _availableConnectionNodes.asStateFlow()

    private val _nodeConnections = MutableStateFlow<List<NodeConnection>>(emptyList())
    val nodeConnections: StateFlow<List<NodeConnection>> = _nodeConnections.asStateFlow()

    private val _connectionSearchQuery = MutableStateFlow("")
    val connectionSearchQuery: StateFlow<String> = _connectionSearchQuery.asStateFlow()

    private val _propertyOptions = MutableStateFlow<List<PropertyOption>>(emptyList())
    val propertyOptions: StateFlow<List<PropertyOption>> = _propertyOptions.asStateFlow()

    private val _apiNodeProperties = MutableStateFlow<List<NodeProperty>>(emptyList())
    val apiNodeProperties: StateFlow<List<NodeProperty>> = _apiNodeProperties.asStateFlow()

    private val _isNodePropertiesLoading = MutableStateFlow(false)
    val isNodePropertiesLoading: StateFlow<Boolean> = _isNodePropertiesLoading.asStateFlow()

    private val _isWikidataPropertiesLoading = MutableStateFlow(false)
    val isWikidataPropertiesLoading: StateFlow<Boolean> = _isWikidataPropertiesLoading.asStateFlow()

    private val _isUpdatingNodeProperties = MutableStateFlow(false)
    val isUpdatingNodeProperties: StateFlow<Boolean> = _isUpdatingNodeProperties.asStateFlow()

    private val _isDeletingNodeProperty = MutableStateFlow(false)
    val isDeletingNodeProperty: StateFlow<Boolean> = _isDeletingNodeProperty.asStateFlow()

    private val _nodePropertiesError = MutableStateFlow<String?>(null)
    val nodePropertiesError: StateFlow<String?> = _nodePropertiesError.asStateFlow()

    private val _nodePropertyDeletionMessage = MutableStateFlow<String?>(null)
    val nodePropertyDeletionMessage: StateFlow<String?> = _nodePropertyDeletionMessage.asStateFlow()

    private val _nodePropertyDeletionError = MutableStateFlow<String?>(null)
    val nodePropertyDeletionError: StateFlow<String?> = _nodePropertyDeletionError.asStateFlow()

    private val _edgeLabelSearchResults = MutableStateFlow<List<WikidataProperty>>(emptyList())
    val edgeLabelSearchResults: StateFlow<List<WikidataProperty>> = _edgeLabelSearchResults.asStateFlow()

    private val _isEdgeLabelSearching = MutableStateFlow(false)
    val isEdgeLabelSearching: StateFlow<Boolean> = _isEdgeLabelSearching.asStateFlow()

    private val _edgeLabelSearchError = MutableStateFlow<String?>(null)
    val edgeLabelSearchError: StateFlow<String?> = _edgeLabelSearchError.asStateFlow()

    private val _isCreatingEdge = MutableStateFlow(false)
    val isCreatingEdge: StateFlow<Boolean> = _isCreatingEdge.asStateFlow()

    private val _edgeCreationError = MutableStateFlow<String?>(null)
    val edgeCreationError: StateFlow<String?> = _edgeCreationError.asStateFlow()

    private val _edgeCreationSuccess = MutableStateFlow<EdgeCreationResult?>(null)
    val edgeCreationSuccess: StateFlow<EdgeCreationResult?> = _edgeCreationSuccess.asStateFlow()

    val filteredConnections: StateFlow<List<NodeConnection>> =
        combine(_connectionSearchQuery, _nodeConnections) { query, connections ->
            if (query.isBlank()) {
                connections
            } else {
                val normalized = query.trim()
                connections.filter { connection ->
                    connection.label.contains(normalized, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _nodeConnections.value
        )

    val reportReasons: List<String> = listOf(
        "Incorrect or misleading information",
        "Offensive or inappropriate content",
        "Duplicate node",
        "Spam or promotional content",
        "Other"
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredOptions: StateFlow<List<PropertyOption>> =
        combine(_searchQuery, _propertyOptions) { query, options ->
            if (query.isBlank()) {
                options
            } else {
                options.filter { option -> option.property.display.contains(query, ignoreCase = true) }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _propertyOptions.value
        )

    init {
        fetchAvailableConnectionNodes()
        fetchNodeProperties()
        fetchWikidataProperties()
        fetchNodeConnections()
    }

    fun retryNodeProperties() {
        fetchNodeProperties()
        fetchWikidataProperties()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateConnectionSearchQuery(query: String) {
        _connectionSearchQuery.value = query
    }

    fun resetConnectionSearchQuery() {
        _connectionSearchQuery.value = ""
    }

    fun togglePropertySelection(statementId: String) {
        _propertyOptions.update { options ->
            options.map { option ->
                if (option.property.statementId == statementId) {
                    option.copy(isChecked = !option.isChecked)
                } else {
                    option
                }
            }
        }
    }

    fun saveSelectedProperties() {
        val selected = _propertyOptions.value
            .filter { it.isChecked }
            .map { it.property }

        viewModelScope.launch {
            _isUpdatingNodeProperties.value = true
            _nodePropertiesError.value = null

            val updateResult = spaceNodeDetailsRepository.updateNodeProperties(spaceId, nodeId, selected)
            updateResult.onSuccess {
                val refreshResult = spaceNodeDetailsRepository.getNodeProperties(spaceId, nodeId)
                refreshResult.onSuccess { properties ->
                    _apiNodeProperties.value = properties
                    syncPropertyOptionsWithSelectedProperties()
                }.onFailure { throwable ->
                    _apiNodeProperties.value = emptyList()
                    _nodePropertiesError.value = throwable.message
                }
            }.onFailure { throwable ->
                _nodePropertiesError.value = throwable.message
            }

            _isUpdatingNodeProperties.value = false
        }
    }

    private fun fetchNodeProperties() {
        viewModelScope.launch {
            _isNodePropertiesLoading.value = true
            _nodePropertiesError.value = null

            val result = spaceNodeDetailsRepository.getNodeProperties(spaceId, nodeId)
            result.onSuccess { properties ->
                _apiNodeProperties.value = properties
                syncPropertyOptionsWithSelectedProperties()
            }.onFailure { throwable ->
                _apiNodeProperties.value = emptyList()
                _nodePropertiesError.value = throwable.message
            }

            _isNodePropertiesLoading.value = false
        }
    }

    private fun fetchWikidataProperties() {
        viewModelScope.launch {
            _isWikidataPropertiesLoading.value = true
            val entityId = _wikidataId.value
            if (entityId.isBlank()) {
                _propertyOptions.value = _apiNodeProperties.value.map { property ->
                    PropertyOption(
                        property = property,
                        isChecked = true
                    )
                }
                _isWikidataPropertiesLoading.value = false
                return@launch
            }

            val result = spaceNodeDetailsRepository.getWikidataEntityProperties(entityId)
            result.onSuccess { properties ->
                updatePropertyOptionsWithCatalog(properties)
                _isWikidataPropertiesLoading.value = false
            }.onFailure {
                // fallback to current selections if catalog fails
                _propertyOptions.value = _apiNodeProperties.value.map { property ->
                    PropertyOption(
                        property = property,
                        isChecked = true
                    )
                }
                _isWikidataPropertiesLoading.value = false
            }
        }
    }

    fun deleteNodeProperty(property: NodeProperty) {
        viewModelScope.launch {
            if (_isDeletingNodeProperty.value) return@launch

            _isDeletingNodeProperty.value = true
            _nodePropertyDeletionMessage.value = null
            _nodePropertyDeletionError.value = null

            val deleteResult =
                spaceNodeDetailsRepository.deleteNodeProperty(spaceId, nodeId, property.statementId)
            deleteResult.onSuccess {
                val refreshResult = spaceNodeDetailsRepository.getNodeProperties(spaceId, nodeId)
                refreshResult.onSuccess { properties ->
                    _apiNodeProperties.value = properties
                    syncPropertyOptionsWithSelectedProperties()
                    _nodePropertyDeletionMessage.value = property.display.ifBlank {
                        if (property.propertyLabel.isNotBlank() && property.valueText.isNotBlank()) {
                            "${property.propertyLabel}: ${property.valueText}"
                        } else {
                            property.propertyLabel.ifBlank { property.statementId }
                        }
                    }
                }.onFailure { throwable ->
                    _nodePropertyDeletionError.value = throwable.message
                }
            }.onFailure { throwable ->
                _nodePropertyDeletionError.value = throwable.message
            }

            _isDeletingNodeProperty.value = false
        }
    }

    private fun fetchAvailableConnectionNodes() {
        viewModelScope.launch {
            val result = spaceNodeDetailsRepository.getSpaceNodes(spaceId)
            result.onSuccess { nodes ->
                val options = nodes
                    .filter { node -> node.id.toString() != nodeId }
                    .sortedBy { it.label.lowercase() }
                    .map { node ->
                        NodeOption(
                            id = node.id.toString(),
                            name = node.label
                        )
                    }
                _availableConnectionNodes.value = options
            }.onFailure {
                if (_availableConnectionNodes.value.isEmpty()) {
                    val fallback = nodeDetailsMap
                        .filterKeys { key -> key != nodeId }
                        .map { (id, details) ->
                            NodeOption(
                                id = id,
                                name = details.name
                            )
                        }
                    _availableConnectionNodes.value = fallback
                }
            }
        }
    }

    private var edgeLabelSearchJob: Job? = null

    fun searchEdgeLabelOptions(query: String) {
        val normalized = query.trim()
        edgeLabelSearchJob?.cancel()

        if (normalized.length < 3) {
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

    fun resetEdgeLabelSearch() {
        edgeLabelSearchJob?.cancel()
        _isEdgeLabelSearching.value = false
        _edgeLabelSearchResults.value = emptyList()
        _edgeLabelSearchError.value = null
    }

    fun clearEdgeLabelSearchError() {
        _edgeLabelSearchError.value = null
    }

    fun clearEdgeCreationError() {
        _edgeCreationError.value = null
    }

    fun clearEdgeCreationSuccess() {
        _edgeCreationSuccess.value = null
    }

    fun addEdge(
        selectedNode: NodeOption,
        isForwardDirection: Boolean,
        label: String,
        wikidataPropertyId: String
    ) {
        if (_isCreatingEdge.value) return

        viewModelScope.launch {
            _isCreatingEdge.value = true
            _edgeCreationError.value = null
            _edgeCreationSuccess.value = null

            val trimmedLabel = label.trim()
            val sourceId = if (isForwardDirection) nodeId else selectedNode.id
            val targetId = if (isForwardDirection) selectedNode.id else nodeId

            val addResult = spaceNodeDetailsRepository.addEdgeToSpaceGraph(
                spaceId = spaceId,
                sourceId = sourceId,
                targetId = targetId,
                label = trimmedLabel,
                wikidataPropertyId = wikidataPropertyId
            )

            val addResponse = addResult.getOrElse { throwable ->
                _edgeCreationError.value = throwable.message
                _isCreatingEdge.value = false
                return@launch
            }

            val snapshotResult = spaceNodeDetailsRepository.createSnapshot(spaceId)
            val snapshotResponse = snapshotResult.getOrElse { throwable ->
                _edgeCreationError.value = throwable.message
                _isCreatingEdge.value = false
                return@launch
            }

            fetchNodeConnections()
            _edgeCreationSuccess.value = EdgeCreationResult(
                edgeId = addResponse.edgeId,
                snapshotId = snapshotResponse.snapshotId,
                isForwardDirection = isForwardDirection,
                connectedNodeName = selectedNode.name,
                label = trimmedLabel
            )

            _isCreatingEdge.value = false
        }
    }

    fun clearNodePropertyDeletionMessage() {
        _nodePropertyDeletionMessage.value = null
    }

    fun clearNodePropertyDeletionError() {
        _nodePropertyDeletionError.value = null
    }

    private fun syncPropertyOptionsWithSelectedProperties() {
        val selectedIds = _apiNodeProperties.value.map { it.statementId }.toSet()
        _propertyOptions.update { options ->
            options.map { option ->
                option.copy(isChecked = selectedIds.contains(option.property.statementId))
            }
        }
    }

    private fun updatePropertyOptionsWithCatalog(catalogProperties: List<NodeProperty>) {
        val merged = (catalogProperties + _apiNodeProperties.value)
            .distinctBy { it.statementId }
        val selectedIds = _apiNodeProperties.value.map { it.statementId }.toSet()
        _propertyOptions.value = merged.map { property ->
            PropertyOption(
                property = property,
                isChecked = selectedIds.contains(property.statementId)
            )
        }
    }

    private fun fetchNodeConnections() {
        viewModelScope.launch {
            val result = spaceNodeDetailsRepository.getSpaceEdges(spaceId)
            result.onSuccess { edges ->
                val filtered = edges.filter { edge ->
                    edge.source.toString() == nodeId || edge.target.toString() == nodeId
                }.map { edge ->
                    NodeConnection(
                        edgeId = edge.id.toString(),
                        label = edge.label.ifBlank { edge.id.toString() },
                        isSource = edge.source.toString() == nodeId
                    )
                }
                _nodeConnections.value = filtered
            }.onFailure {
                _nodeConnections.value = emptyList()
            }
        }
    }

    companion object {
        private val nodeDetailsMap = mapOf(
            "1" to NodeDetailsMock(
                name = "Airport",
                wikidataId = "Q4808437",
                availableProperties = listOf(
                    "Location: Istanbul",
                    "Founded In: 2010 January",
                    "Long Name: Istanbul Sabiha Gokcen Uluslararasi Havalimani",
                    "Size: 100000 m2",
                    "Has flight capacity: 500",
                    "Has parking capacity: 1200",
                    "Has helipad",
                    "Supports international flights",
                    "Has VIP lounge"
                ),
                defaultProperties = listOf(
                    "Location: Istanbul",
                    "Founded In: 2010 January",
                    "Long Name: Istanbul Sabiha Gokcen Uluslararasi Havalimani"
                )
            ),
            "2" to NodeDetailsMock(
                name = "Railway Station",
                wikidataId = "Q123456",
                availableProperties = listOf(
                    "Location: Istanbul",
                    "Platforms: 8",
                    "Built In: 1920",
                    "Has high-speed rail service",
                    "Offers luggage storage",
                    "Connected to metro line"
                ),
                defaultProperties = listOf(
                    "Location: Istanbul",
                    "Platforms: 8",
                    "Has high-speed rail service"
                )
            ),
            "3" to NodeDetailsMock(
                name = "City Center",
                wikidataId = "Q654321",
                availableProperties = listOf(
                    "Location: Downtown",
                    "Has public square",
                    "Main shopping district",
                    "Historic landmark nearby",
                    "Hosts cultural events"
                ),
                defaultProperties = listOf(
                    "Location: Downtown",
                    "Main shopping district",
                    "Hosts cultural events"
                )
            ),
            "4" to NodeDetailsMock(
                name = "Museum",
                wikidataId = "Q234567",
                availableProperties = listOf(
                    "Location: Historical district",
                    "Founded In: 1950",
                    "Collection Size: 20000 artifacts",
                    "Hosts guided tours",
                    "Has cafe",
                    "Open daily"
                ),
                defaultProperties = listOf(
                    "Location: Historical district",
                    "Collection Size: 20000 artifacts",
                    "Hosts guided tours"
                )
            ),
            "5" to NodeDetailsMock(
                name = "University",
                wikidataId = "Q345678",
                availableProperties = listOf(
                    "Location: Campus Avenue",
                    "Founded In: 1975",
                    "Student Population: 25000",
                    "Research Centers: 12",
                    "Offers graduate programs",
                    "Has student housing"
                ),
                defaultProperties = listOf(
                    "Location: Campus Avenue",
                    "Student Population: 25000",
                    "Offers graduate programs"
                )
            ),
            "6" to NodeDetailsMock(
                name = "Library",
                wikidataId = "Q456789",
                availableProperties = listOf(
                    "Location: Main Street",
                    "Founded In: 1985",
                    "Book Collection: 150000",
                    "Has reading rooms",
                    "Provides digital archives",
                    "Open on weekends"
                ),
                defaultProperties = listOf(
                    "Location: Main Street",
                    "Book Collection: 150000",
                    "Provides digital archives"
                )
            ),
            "7" to NodeDetailsMock(
                name = "Park",
                wikidataId = "Q567890",
                availableProperties = listOf(
                    "Location: Riverside",
                    "Size: 80 hectares",
                    "Has playground",
                    "Hosts festivals",
                    "Has walking trails",
                    "Pet friendly"
                ),
                defaultProperties = listOf(
                    "Location: Riverside",
                    "Has playground",
                    "Has walking trails"
                )
            ),
            "8" to NodeDetailsMock(
                name = "Sports Arena",
                wikidataId = "Q678901",
                availableProperties = listOf(
                    "Location: Arena Boulevard",
                    "Seating Capacity: 25000",
                    "Supports indoor events",
                    "Has VIP suites",
                    "Hosts concerts",
                    "Has training facilities"
                ),
                defaultProperties = listOf(
                    "Location: Arena Boulevard",
                    "Seating Capacity: 25000",
                    "Hosts concerts"
                )
            )
        )
    }
}

