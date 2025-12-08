package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.network.dto.CountryPosition
import com.yybb.myapplication.data.network.dto.ReportReasonItem
import com.yybb.myapplication.data.repository.CountriesRepository
import com.yybb.myapplication.data.repository.SpaceNodeDetailsRepository
import com.yybb.myapplication.data.repository.SpacesRepository
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
    private val spacesRepository: SpacesRepository,
    private val countriesRepository: CountriesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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
        val isSource: Boolean,
        val sourceId: String,
        val targetId: String
    )

    data class EdgeCreationResult(
        val edgeId: Int,
        val snapshotId: Int,
        val isForwardDirection: Boolean,
        val connectedNodeName: String,
        val label: String
    )

    private val spaceId: String = checkNotNull(savedStateHandle["spaceId"])
    val nodeId: String = checkNotNull(savedStateHandle["nodeId"])
    private val nodeLabelArg: String? = savedStateHandle["nodeLabel"]
    private val nodeWikidataIdArg: String? = savedStateHandle["nodeWikidataId"]

    private val _nodeName = MutableStateFlow(nodeLabelArg ?: "")
    val nodeName: StateFlow<String> = _nodeName.asStateFlow()

    private val _wikidataId = MutableStateFlow(
        nodeWikidataIdArg?.takeUnless { it.isBlank() } ?: ""
    )
    val wikidataId: StateFlow<String> = _wikidataId.asStateFlow()

    private val _criticalError = MutableStateFlow<String?>(null)
    val criticalError: StateFlow<String?> = _criticalError.asStateFlow()

    fun clearCriticalError() {
        _criticalError.value = null
    }

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

    private val _wikidataPropertiesError = MutableStateFlow<String?>(null)
    val wikidataPropertiesError: StateFlow<String?> = _wikidataPropertiesError.asStateFlow()

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

    private val _isDeletingNode = MutableStateFlow(false)
    val isDeletingNode: StateFlow<Boolean> = _isDeletingNode.asStateFlow()

    private val _deleteNodeError = MutableStateFlow<String?>(null)
    val deleteNodeError: StateFlow<String?> = _deleteNodeError.asStateFlow()

    private val _deleteNodeSuccess = MutableStateFlow<Boolean>(false)
    val deleteNodeSuccess: StateFlow<Boolean> = _deleteNodeSuccess.asStateFlow()

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

    private val _isLoadingReportReasons = MutableStateFlow(false)
    val isLoadingReportReasons: StateFlow<Boolean> = _isLoadingReportReasons.asStateFlow()

    private val _reportReasons = MutableStateFlow<List<ReportReasonItem>>(emptyList())
    val reportReasons: StateFlow<List<ReportReasonItem>> = _reportReasons.asStateFlow()

    private val _isSubmittingReport = MutableStateFlow(false)
    val isSubmittingReport: StateFlow<Boolean> = _isSubmittingReport.asStateFlow()

    private val _reportSubmitSuccess = MutableStateFlow<Boolean>(false)
    val reportSubmitSuccess: StateFlow<Boolean> = _reportSubmitSuccess.asStateFlow()

    private val _reportError = MutableStateFlow<String?>(null)
    val reportError: StateFlow<String?> = _reportError.asStateFlow()

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

    // Location state
    private val _locationName = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    private val _nodeDetails = MutableStateFlow<com.yybb.myapplication.data.model.SpaceNode?>(null)
    val nodeDetails: StateFlow<com.yybb.myapplication.data.model.SpaceNode?> = _nodeDetails.asStateFlow()

    private val _showEditLocationDialog = MutableStateFlow(false)
    val showEditLocationDialog: StateFlow<Boolean> = _showEditLocationDialog.asStateFlow()

    private val _countries = MutableStateFlow<List<CountryPosition>>(emptyList())
    val countries: StateFlow<List<CountryPosition>> = _countries.asStateFlow()

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities: StateFlow<List<String>> = _cities.asStateFlow()

    private val _isLoadingCountries = MutableStateFlow(false)
    val isLoadingCountries: StateFlow<Boolean> = _isLoadingCountries.asStateFlow()

    private val _isLoadingCities = MutableStateFlow(false)
    val isLoadingCities: StateFlow<Boolean> = _isLoadingCities.asStateFlow()

    private val _isGettingCoordinates = MutableStateFlow(false)
    val isGettingCoordinates: StateFlow<Boolean> = _isGettingCoordinates.asStateFlow()

    private val _isUpdatingLocation = MutableStateFlow(false)
    val isUpdatingLocation: StateFlow<Boolean> = _isUpdatingLocation.asStateFlow()

    private val _locationUpdateError = MutableStateFlow<String?>(null)
    val locationUpdateError: StateFlow<String?> = _locationUpdateError.asStateFlow()

    init {
        // Validate required data
        if (nodeLabelArg.isNullOrBlank()) {
            _criticalError.value = "Node name is required but not provided"
        } else {
            fetchAvailableConnectionNodes()
            fetchNodeProperties()
            fetchWikidataProperties()
            fetchNodeConnections()
            fetchNodeDetails()
            loadCountries()
        }
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

            val isInitialLoad = _apiNodeProperties.value.isEmpty()
            val result = spaceNodeDetailsRepository.getNodeProperties(spaceId, nodeId)
            result.onSuccess { properties ->
                _apiNodeProperties.value = properties
                syncPropertyOptionsWithSelectedProperties()
                _isNodePropertiesLoading.value = false
            }.onFailure { throwable ->
                _apiNodeProperties.value = emptyList()
                if (isInitialLoad) {
                    // Critical error on initial load - show error dialog
                    _criticalError.value = throwable.message ?: "Failed to load node properties"
                } else {
                    // Non-critical error on subsequent loads
                    _nodePropertiesError.value = throwable.message
                }
                _isNodePropertiesLoading.value = false
            }
        }
    }

    private fun fetchWikidataProperties() {
        viewModelScope.launch {
            _isWikidataPropertiesLoading.value = true
            _wikidataPropertiesError.value = null
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
                _wikidataPropertiesError.value = null
                _isWikidataPropertiesLoading.value = false
            }.onFailure {
                // Set error message when catalog fails
                _wikidataPropertiesError.value = "Could not retrieve the properties. Please go back and try again."
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
                // Don't use fallback - just keep empty list
                _availableConnectionNodes.value = emptyList()
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

    fun deleteNode() {
        if (_isDeletingNode.value) return

        viewModelScope.launch {
            _isDeletingNode.value = true
            _deleteNodeError.value = null
            _deleteNodeSuccess.value = false

            val deleteResult = spaceNodeDetailsRepository.deleteNode(spaceId, nodeId)
            val deleteResponse = deleteResult.getOrElse { throwable ->
                _deleteNodeError.value = throwable.message
                _isDeletingNode.value = false
                return@launch
            }

            val snapshotResult = spaceNodeDetailsRepository.createSnapshot(spaceId)
            val snapshotResponse = snapshotResult.getOrElse { throwable ->
                _deleteNodeError.value = throwable.message
                _isDeletingNode.value = false
                return@launch
            }

            _deleteNodeSuccess.value = true
            _isDeletingNode.value = false
        }
    }

    fun clearDeleteNodeError() {
        _deleteNodeError.value = null
    }

    fun clearDeleteNodeSuccess() {
        _deleteNodeSuccess.value = false
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

    private var fetchNodeConnectionsJob: Job? = null

    fun refreshNodeConnections() {
        // Cancel previous job if it's still running to avoid duplicate calls
        fetchNodeConnectionsJob?.cancel()
        fetchNodeConnectionsJob = null
        fetchNodeConnections()
    }

    private fun fetchNodeConnections() {
        fetchNodeConnectionsJob = viewModelScope.launch {
            val result = spaceNodeDetailsRepository.getSpaceEdges(spaceId)
            result.onSuccess { edges ->
                val filtered = edges.filter { edge ->
                    edge.source.toString() == nodeId || edge.target.toString() == nodeId
                }.map { edge ->
                    NodeConnection(
                        edgeId = edge.id.toString(),
                        label = edge.label.ifBlank { edge.id.toString() },
                        isSource = edge.source.toString() == nodeId,
                        sourceId = edge.source.toString(),
                        targetId = edge.target.toString()
                    )
                }
                _nodeConnections.value = filtered
            }.onFailure {
                _nodeConnections.value = emptyList()
            }
            fetchNodeConnectionsJob = null
        }
    }

    fun fetchReportReasons() {
        viewModelScope.launch {
            _isLoadingReportReasons.value = true
            _reportError.value = null

            val result = spacesRepository.getReportReasons("node")
            if (result.isSuccess) {
                _reportReasons.value = result.getOrNull() ?: emptyList()
            } else {
                _reportError.value = result.exceptionOrNull()?.message ?: "Failed to load report reasons"
            }
            _isLoadingReportReasons.value = false
        }
    }

    fun submitReport(reason: String) {
        viewModelScope.launch {
            _isSubmittingReport.value = true
            _reportError.value = null

            val result = spacesRepository.submitReport(
                contentType = "node",
                contentId = nodeId.toIntOrNull() ?: 0,
                reason = reason
            )
            if (result.isSuccess) {
                _reportSubmitSuccess.value = true
            } else {
                _reportSubmitSuccess.value = false
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

    // Location functions
    fun showEditLocationDialog() {
        _showEditLocationDialog.value = true
    }

    fun hideEditLocationDialog() {
        _showEditLocationDialog.value = false
    }

    fun loadCountries() {
        viewModelScope.launch {
            _isLoadingCountries.value = true
            countriesRepository.getCountries()
                .onSuccess { countriesList ->
                    _countries.value = countriesList.sortedBy { it.name }
                    _isLoadingCountries.value = false
                }
                .onFailure {
                    _isLoadingCountries.value = false
                }
        }
    }

    fun loadCities(country: String) {
        viewModelScope.launch {
            _isLoadingCities.value = true
            _cities.value = emptyList()
            countriesRepository.getCities(country)
                .onSuccess { citiesList ->
                    _cities.value = citiesList.sorted()
                    _isLoadingCities.value = false
                }
                .onFailure {
                    _isLoadingCities.value = false
                }
        }
    }

    private val _coordinatesResult = MutableStateFlow<com.yybb.myapplication.data.network.dto.NominatimCoordinates?>(null)
    val coordinatesResult: StateFlow<com.yybb.myapplication.data.network.dto.NominatimCoordinates?> = _coordinatesResult.asStateFlow()

    fun getCoordinatesFromAddress(city: String?, country: String?) {
        if (city == null || country == null) {
            _coordinatesResult.value = null
            return
        }

        viewModelScope.launch {
            _isGettingCoordinates.value = true
            _locationUpdateError.value = null
            _coordinatesResult.value = null
            val query = "$city, $country"
            val result = spaceNodeDetailsRepository.getCoordinatesFromAddress(query)
            _isGettingCoordinates.value = false
            result.onSuccess { coordinates ->
                _coordinatesResult.value = coordinates
            }.onFailure { throwable ->
                _coordinatesResult.value = null
                _locationUpdateError.value = throwable.message ?: "Failed to get coordinates"
            }
        }
    }

    fun updateNodeLocation(
        country: String?,
        city: String?,
        locationName: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        if (_isUpdatingLocation.value) return
        
        viewModelScope.launch {
            _isUpdatingLocation.value = true
            _locationUpdateError.value = null

            try {
                val updateResult = spaceNodeDetailsRepository.updateNodeLocation(
                    spaceId = spaceId,
                    nodeId = nodeId,
                    country = country,
                    city = city,
                    locationName = locationName,
                    latitude = latitude,
                    longitude = longitude
                )

                updateResult.onSuccess {
                    // Create snapshot
                    val snapshotResult = spaceNodeDetailsRepository.createSnapshot(spaceId)
                    snapshotResult.onSuccess {
                        // Refresh node details by calling getSpaceNodes and finding matching node
                        refreshNodeDetailsFromSpaceNodes()
                        _isUpdatingLocation.value = false
                        _showEditLocationDialog.value = false
                    }.onFailure { throwable ->
                        _locationUpdateError.value = throwable.message ?: "Failed to create snapshot"
                        _isUpdatingLocation.value = false
                    }
                }.onFailure { throwable ->
                    _locationUpdateError.value = throwable.message ?: "Failed to update location"
                    _isUpdatingLocation.value = false
                }
            } catch (e: Exception) {
                _locationUpdateError.value = e.message ?: "An unexpected error occurred"
                _isUpdatingLocation.value = false
            }
        }
    }

    private suspend fun refreshNodeDetailsFromSpaceNodes() {
        val result = spaceNodeDetailsRepository.getSpaceNodes(spaceId)
        result.onSuccess { nodes ->
            // First try to find by wikidata_id if available
            val currentWikidataId = _wikidataId.value
            val matchingNode = if (currentWikidataId.isNotBlank()) {
                nodes.find { it.wikidataId == currentWikidataId }
            } else {
                // Fallback to matching by nodeId if wikidata_id is not available
                nodes.find { it.id.toString() == nodeId }
            }
            
            if (matchingNode != null) {
                _nodeDetails.value = matchingNode
                _locationName.value = matchingNode.locationName
                // Update node name if it has changed
                if (matchingNode.label.isNotBlank()) {
                    _nodeName.value = matchingNode.label
                }
            }
        }.onFailure {
            // Silently fail - location is optional
        }
    }

    private fun fetchNodeDetails() {
        viewModelScope.launch {
            refreshNodeDetailsFromSpaceNodes()
        }
    }

    fun clearLocationUpdateError() {
        _locationUpdateError.value = null
    }
}

