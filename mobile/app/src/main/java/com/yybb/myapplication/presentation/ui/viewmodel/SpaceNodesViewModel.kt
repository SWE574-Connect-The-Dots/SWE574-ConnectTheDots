package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.SpaceEdge
import com.yybb.myapplication.data.repository.SpaceNodesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpaceNodesViewModel @Inject constructor(
    private val spaceNodesRepository: SpaceNodesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spaceId: String = checkNotNull(savedStateHandle["spaceId"])

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _nodes = MutableStateFlow<List<SpaceNode>>(emptyList())
    val nodes: StateFlow<List<SpaceNode>> = _nodes.asStateFlow()

    private val _filteredNodes = MutableStateFlow<List<SpaceNode>>(emptyList())
    val filteredNodes: StateFlow<List<SpaceNode>> = _filteredNodes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var isFirstResume = true

    init {
        observeFilters()
        fetchSpaceNodes()
    }
    
    fun onScreenResumed() {
        // Skip the first resume to avoid duplicate call with init block
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        // Only refresh if not currently loading
        if (!_isLoading.value) {
            fetchSpaceNodes()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun retry() {
        fetchSpaceNodes()
    }

    fun refresh() {
        fetchSpaceNodes()
    }

    private fun fetchSpaceNodes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val nodesResult = spaceNodesRepository.getSpaceNodes(spaceId)
            val edgesResult = spaceNodesRepository.getSpaceEdges(spaceId)

            when {
                nodesResult.isSuccess && edgesResult.isSuccess -> {
                    val nodes = nodesResult.getOrNull() ?: emptyList()
                    val edges = edgesResult.getOrNull() ?: emptyList()
                    
                    // Calculate connection counts for each node
                    val nodesWithCounts = calculateConnectionCounts(nodes, edges)
                    
                    // Sort nodes by connection count (descending)
                    val sortedNodes = nodesWithCounts.sortedByDescending { it.connectionCount }
                    
                    _nodes.value = sortedNodes
                }
                nodesResult.isFailure -> {
                    _nodes.value = emptyList()
                    _errorMessage.value = nodesResult.exceptionOrNull()?.message
                }
                edgesResult.isFailure -> {
                    // If edges fail, still show nodes but without connection counts
                    val nodes = nodesResult.getOrNull() ?: emptyList()
                    _nodes.value = nodes.sortedByDescending { it.connectionCount }
                    // Don't set error message for edges failure, just log it
                }
            }

            _isLoading.value = false
        }
    }

    private fun calculateConnectionCounts(
        nodes: List<SpaceNode>,
        edges: List<SpaceEdge>
    ): List<SpaceNode> {
        // Create a map to count connections for each node
        val connectionCountMap = mutableMapOf<Int, Int>()
        
        // Initialize all nodes with 0 connections
        nodes.forEach { node ->
            connectionCountMap[node.id] = 0
        }
        
        // Count connections: if a node is source or target of an edge, increment its count
        edges.forEach { edge ->
            connectionCountMap[edge.source] = (connectionCountMap[edge.source] ?: 0) + 1
            connectionCountMap[edge.target] = (connectionCountMap[edge.target] ?: 0) + 1
        }
        
        // Update nodes with their connection counts
        return nodes.map { node ->
            node.copy(connectionCount = connectionCountMap[node.id] ?: 0)
        }
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
        val filtered = if (query.isBlank()) {
            nodes
        } else {
            val normalizedQuery = query.trim()
            nodes.filter { node ->
                node.label.contains(normalizedQuery, ignoreCase = true) ||
                    node.locationName?.contains(normalizedQuery, ignoreCase = true) == true ||
                    node.city?.contains(normalizedQuery, ignoreCase = true) == true ||
                    node.country?.contains(normalizedQuery, ignoreCase = true) == true ||
                    node.district?.contains(normalizedQuery, ignoreCase = true) == true ||
                    node.street?.contains(normalizedQuery, ignoreCase = true) == true
            }
        }
        // Maintain sort order by connection count after filtering
        return filtered.sortedByDescending { it.connectionCount }
    }
}

