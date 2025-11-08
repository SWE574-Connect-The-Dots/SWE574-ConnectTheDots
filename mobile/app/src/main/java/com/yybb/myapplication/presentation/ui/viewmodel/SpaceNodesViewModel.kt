package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.SpaceNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpaceNodesViewModel @Inject constructor() : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val mockNodes = listOf(
        SpaceNode(
            id = "1",
            name = "Airport",
            description = "Primary international airport hub for the city.",
            connectionCount = 3
        ),
        SpaceNode(
            id = "2",
            name = "Railway Station",
            description = "Central station linking regional and intercity trains.",
            connectionCount = 2
        ),
        SpaceNode(
            id = "3",
            name = "City Center",
            description = "Main downtown district with commercial landmarks.",
            connectionCount = 5
        ),
        SpaceNode(
            id = "4",
            name = "Museum",
            description = "Historic museum showcasing cultural artifacts.",
            connectionCount = 1
        ),
        SpaceNode(
            id = "5",
            name = "University",
            description = "Leading academic campus with research facilities.",
            connectionCount = 4
        ),
        SpaceNode(
            id = "6",
            name = "Library",
            description = "Public library providing community learning spaces.",
            connectionCount = 2
        ),
        SpaceNode(
            id = "7",
            name = "Park",
            description = "Urban park with walking trails and recreation areas.",
            connectionCount = 3
        ),
        SpaceNode(
            id = "8",
            name = "Sports Arena",
            description = "Multipurpose arena hosting sporting and music events.",
            connectionCount = 2
        )
    )

    private val _nodes = MutableStateFlow(mockNodes)
    val nodes: StateFlow<List<SpaceNode>> = _nodes.asStateFlow()

    private val _filteredNodes = MutableStateFlow<List<SpaceNode>>(emptyList())
    val filteredNodes: StateFlow<List<SpaceNode>> = _filteredNodes.asStateFlow()

    init {
        observeFilters()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun observeFilters() {
        viewModelScope.launch {
            combine(_searchQuery, _nodes) { query, nodes ->
                filterNodes(nodes, query)
            }.collect { filtered ->
                _filteredNodes.value = filtered
            }
        }
    }

    private fun filterNodes(nodes: List<SpaceNode>, query: String): List<SpaceNode> {
        if (query.isBlank()) {
            return nodes
        }
        val normalizedQuery = query.trim()
        return nodes.filter { node ->
            node.name.contains(normalizedQuery, ignoreCase = true) ||
                node.description.contains(normalizedQuery, ignoreCase = true)
        }
    }
}

