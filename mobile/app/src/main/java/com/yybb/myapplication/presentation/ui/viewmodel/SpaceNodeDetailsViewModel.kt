package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SpaceNodeDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class NodeDetailsMock(
        val name: String,
        val wikidataId: String,
        val availableProperties: List<String>,
        val defaultProperties: List<String>
    )

    data class PropertyOption(
        val label: String,
        val isChecked: Boolean
    )

    data class NodeOption(
        val id: String,
        val name: String
    )

    private val nodeId: String = checkNotNull(savedStateHandle["nodeId"])

    private val nodeDetails: NodeDetailsMock = nodeDetailsMap[nodeId] ?: nodeDetailsMap.values.first()

    private val _nodeName = MutableStateFlow(nodeDetails.name)
    val nodeName: StateFlow<String> = _nodeName.asStateFlow()

    private val _wikidataId = MutableStateFlow(nodeDetails.wikidataId)
    val wikidataId: StateFlow<String> = _wikidataId.asStateFlow()

    private val _nodeProperties = MutableStateFlow(nodeDetails.defaultProperties)
    val nodeProperties: StateFlow<List<String>> = _nodeProperties.asStateFlow()

    private val _propertyOptions = MutableStateFlow(
        nodeDetails.availableProperties.map { property ->
            PropertyOption(
                label = property,
                isChecked = nodeDetails.defaultProperties.contains(property)
            )
        }
    )

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
                options.filter { it.label.contains(query, ignoreCase = true) }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _propertyOptions.value
        )

    init {
        // No-op, flows initialized
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun togglePropertySelection(propertyLabel: String) {
        _propertyOptions.update { options ->
            options.map { option ->
                if (option.label == propertyLabel) {
                    option.copy(isChecked = !option.isChecked)
                } else {
                    option
                }
            }
        }
    }

    fun removeProperty(propertyLabel: String) {
        _nodeProperties.update { properties ->
            properties.filterNot { it == propertyLabel }
        }
        _propertyOptions.update { options ->
            options.map { option ->
                if (option.label == propertyLabel) {
                    option.copy(isChecked = false)
                } else {
                    option
                }
            }
        }
    }

    fun saveSelectedProperties() {
        val selected = _propertyOptions.value
            .filter { it.isChecked }
            .map { it.label }
        _nodeProperties.value = selected
    }

    fun searchEdgeLabelOptions(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        return nodeDetails.availableProperties.filter { it.contains(query, ignoreCase = true) }
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

