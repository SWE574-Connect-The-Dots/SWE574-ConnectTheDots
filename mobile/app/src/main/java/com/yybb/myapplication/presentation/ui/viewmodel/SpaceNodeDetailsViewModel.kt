package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.repository.SpaceNodeDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
        val targetNodeId: String,
        val targetNodeName: String,
        val edgeDescription: String
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

    private val _availableConnectionNodes = MutableStateFlow(
        nodeDetailsMap
            .filterKeys { it != nodeId }
            .map { entry ->
                NodeOption(
                    id = entry.key,
                    name = entry.value.name
                )
            }
    )
    val availableConnectionNodes: StateFlow<List<NodeOption>> = _availableConnectionNodes.asStateFlow()

    private val _nodeConnections = MutableStateFlow(connectionsMap[nodeId] ?: emptyList())
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

    private val _nodePropertiesError = MutableStateFlow<String?>(null)
    val nodePropertiesError: StateFlow<String?> = _nodePropertiesError.asStateFlow()

    val filteredConnections: StateFlow<List<NodeConnection>> =
        combine(_connectionSearchQuery, _nodeConnections) { query, connections ->
            if (query.isBlank()) {
                connections
            } else {
                val normalized = query.trim()
                connections.filter { connection ->
                    connection.targetNodeName.contains(normalized, ignoreCase = true) ||
                        connection.edgeDescription.contains(normalized, ignoreCase = true)
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
        fetchNodeProperties()
        fetchWikidataProperties()
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
        _apiNodeProperties.value = selected
        syncPropertyOptionsWithSelectedProperties()
    }

    fun searchEdgeLabelOptions(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        val options = _propertyOptions.value
        return if (options.isNotEmpty()) {
            options.map { it.property.display }
                .filter { it.contains(query, ignoreCase = true) }
        } else {
            nodeDetails.availableProperties.filter { it.contains(query, ignoreCase = true) }
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

    fun removeApiProperty(statementId: String) {
        _apiNodeProperties.update { properties ->
            properties.filterNot { it.statementId == statementId }
        }
        _propertyOptions.update { options ->
            options.map { option ->
                if (option.property.statementId == statementId) {
                    option.copy(isChecked = false)
                } else {
                    option
                }
            }
        }
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

        private val connectionsMap: Map<String, List<NodeConnection>> = mapOf(
            "1" to listOf(
                NodeConnection("2", nodeDetailsMap["2"]?.name ?: "Railway Station", "Airport supports high-speed rail transfers for Railway Station passengers."),
                NodeConnection("3", nodeDetailsMap["3"]?.name ?: "City Center", "Airport provides shuttle services directly to City Center."),
                NodeConnection("4", nodeDetailsMap["4"]?.name ?: "Museum", "Airport collaborates with the Museum on aviation history exhibits."),
                NodeConnection("5", nodeDetailsMap["5"]?.name ?: "University", "Airport hosts University-led research on transportation innovation."),
                NodeConnection("6", nodeDetailsMap["6"]?.name ?: "Library", "Airport archives are mirrored at the Library for public access."),
                NodeConnection("7", nodeDetailsMap["7"]?.name ?: "Park", "Airport green initiatives support tree planting efforts in the Park."),
                NodeConnection("8", nodeDetailsMap["8"]?.name ?: "Sports Arena", "Airport manages charter flights for major Sports Arena events.")
            ),
            "2" to listOf(
                NodeConnection("1", nodeDetailsMap["1"]?.name ?: "Airport", "Railway Station offers direct train lines connecting to the Airport terminals."),
                NodeConnection("3", nodeDetailsMap["3"]?.name ?: "City Center", "Railway Station serves commuters traveling towards City Center business districts."),
                NodeConnection("4", nodeDetailsMap["4"]?.name ?: "Museum", "Railway Station provides weekend tour lines to the Museum district."),
                NodeConnection("5", nodeDetailsMap["5"]?.name ?: "University", "Railway Station commuter services link students to the University campus."),
                NodeConnection("6", nodeDetailsMap["6"]?.name ?: "Library", "Railway Station offers luggage storage near the Library entrance."),
                NodeConnection("7", nodeDetailsMap["7"]?.name ?: "Park", "Railway Station organizes seasonal excursions to the Park."),
                NodeConnection("8", nodeDetailsMap["8"]?.name ?: "Sports Arena", "Railway Station charters group travel to large Sports Arena events.")
            ),
            "3" to listOf(
                NodeConnection("1", nodeDetailsMap["1"]?.name ?: "Airport", "City Center tourist routes originate from Airport arrivals."),
                NodeConnection("2", nodeDetailsMap["2"]?.name ?: "Railway Station", "City Center relies on Railway Station for peak commuter inflows."),
                NodeConnection("4", nodeDetailsMap["4"]?.name ?: "Museum", "City Center cultural pathways guide visitors to the Museum exhibits."),
                NodeConnection("5", nodeDetailsMap["5"]?.name ?: "University", "City Center hosts University student showcases in public plazas."),
                NodeConnection("6", nodeDetailsMap["6"]?.name ?: "Library", "City Center literature festivals partner with the Library archives."),
                NodeConnection("7", nodeDetailsMap["7"]?.name ?: "Park", "City Center weekend markets connect to Park recreational areas."),
                NodeConnection("8", nodeDetailsMap["8"]?.name ?: "Sports Arena", "City Center fan experiences precede Sports Arena championship games.")
            ),
            "4" to listOf(
                NodeConnection("1", nodeDetailsMap["1"]?.name ?: "Airport", "Museum loans aviation artifacts displayed at the Airport welcome hall."),
                NodeConnection("2", nodeDetailsMap["2"]?.name ?: "Railway Station", "Museum pop-up kiosks appear in Railway Station concourses during exhibits."),
                NodeConnection("3", nodeDetailsMap["3"]?.name ?: "City Center", "Museum partners with City Center events for shared cultural programs."),
                NodeConnection("5", nodeDetailsMap["5"]?.name ?: "University", "Museum collaborates with University research teams on historical archives."),
                NodeConnection("6", nodeDetailsMap["6"]?.name ?: "Library", "Museum and Library co-curate themed reading lists for exhibits."),
                NodeConnection("7", nodeDetailsMap["7"]?.name ?: "Park", "Museum outdoor installations extend into the Park pathways."),
                NodeConnection("8", nodeDetailsMap["8"]?.name ?: "Sports Arena", "Museum hosts pre-event history tours for Sports Arena audiences.")
            ),
            "5" to listOf(
                NodeConnection("1", nodeDetailsMap["1"]?.name ?: "Airport", "University aviation programs coordinate internships at the Airport."),
                NodeConnection("2", nodeDetailsMap["2"]?.name ?: "Railway Station", "University student transit passes include access via the Railway Station."),
                NodeConnection("3", nodeDetailsMap["3"]?.name ?: "City Center", "University cultural festivals extend into City Center venues."),
                NodeConnection("4", nodeDetailsMap["4"]?.name ?: "Museum", "University history departments curate exhibits within the Museum."),
                NodeConnection("6", nodeDetailsMap["6"]?.name ?: "Library", "University research findings are archived at the Library."),
                NodeConnection("7", nodeDetailsMap["7"]?.name ?: "Park", "University wellness events frequently take place in the Park."),
                NodeConnection("8", nodeDetailsMap["8"]?.name ?: "Sports Arena", "University athletic teams host championship games at the Sports Arena.")
            ),
            "6" to listOf(
                NodeConnection("1", nodeDetailsMap["1"]?.name ?: "Airport", "Library digitization labs process Airport historical flight logs."),
                NodeConnection("2", nodeDetailsMap["2"]?.name ?: "Railway Station", "Library reading programs advertise in Railway Station waiting areas."),
                NodeConnection("3", nodeDetailsMap["3"]?.name ?: "City Center", "Library mobile kiosks appear in City Center during book festivals."),
                NodeConnection("4", nodeDetailsMap["4"]?.name ?: "Museum", "Library and Museum share archival preservation workshops."),
                NodeConnection("5", nodeDetailsMap["5"]?.name ?: "University", "Library provides extended research hours for University scholars."),
                NodeConnection("7", nodeDetailsMap["7"]?.name ?: "Park", "Library hosts outdoor reading circles in the Park."),
                NodeConnection("8", nodeDetailsMap["8"]?.name ?: "Sports Arena", "Library curates sports history displays at the Sports Arena.")
            ),
            "7" to listOf(
                NodeConnection("1", nodeDetailsMap["1"]?.name ?: "Airport", "Park shuttle stops receive visitors directly from the Airport."),
                NodeConnection("2", nodeDetailsMap["2"]?.name ?: "Railway Station", "Park festival trains depart from Railway Station platforms."),
                NodeConnection("3", nodeDetailsMap["3"]?.name ?: "City Center", "Park weekend markets are promoted through City Center events."),
                NodeConnection("4", nodeDetailsMap["4"]?.name ?: "Museum", "Park sculpture trails connect to Museum art residencies."),
                NodeConnection("5", nodeDetailsMap["5"]?.name ?: "University", "Park environmental studies involve University volunteer programs."),
                NodeConnection("6", nodeDetailsMap["6"]?.name ?: "Library", "Park literacy days feature pop-up libraries run by the Library."),
                NodeConnection("8", nodeDetailsMap["8"]?.name ?: "Sports Arena", "Park fitness trails are used by teams training for Sports Arena events.")
            ),
            "8" to listOf(
                NodeConnection("1", nodeDetailsMap["1"]?.name ?: "Airport", "Sports Arena manages international athlete arrivals through the Airport."),
                NodeConnection("2", nodeDetailsMap["2"]?.name ?: "Railway Station", "Sports Arena coordinates fan express routes from the Railway Station."),
                NodeConnection("3", nodeDetailsMap["3"]?.name ?: "City Center", "Sports Arena events boost nightlife across City Center neighborhoods."),
                NodeConnection("4", nodeDetailsMap["4"]?.name ?: "Museum", "Sports Arena co-hosts memorabilia exhibits with the Museum."),
                NodeConnection("5", nodeDetailsMap["5"]?.name ?: "University", "Sports Arena collaborates with University athletic departments for tournaments."),
                NodeConnection("6", nodeDetailsMap["6"]?.name ?: "Library", "Sports Arena archives sports records with the Library's digital collections."),
                NodeConnection("7", nodeDetailsMap["7"]?.name ?: "Park", "Sports Arena wellness campaigns host outdoor sessions in the Park.")
            )
        )
    }
}

