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

enum class NodeSortOrder {
    DATE_ASC,           // Order by created at date ascending (oldest first)
    DATE_DESC,          // Order by created at date descending (newest first)
    CONNECTION_ASC,     // Order by connection size ascending (lowest first)
    CONNECTION_DESC     // Order by connection size descending (highest first)
}

@HiltViewModel
class SpaceNodesViewModel @Inject constructor(
    private val spaceNodesRepository: SpaceNodesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spaceId: String = checkNotNull(savedStateHandle["spaceId"])

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(NodeSortOrder.CONNECTION_DESC)
    val sortOrder: StateFlow<NodeSortOrder> = _sortOrder.asStateFlow()

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

    fun setSortOrder(order: NodeSortOrder) {
        _sortOrder.value = order
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
                    
                    // Store unsorted nodes - sorting will be applied in observeFilters based on current sort order
                    _nodes.value = nodesWithCounts
                }
                nodesResult.isFailure -> {
                    _nodes.value = emptyList()
                    _errorMessage.value = nodesResult.exceptionOrNull()?.message
                }
                edgesResult.isFailure -> {
                    // If edges fail, still show nodes but without connection counts
                    val nodes = nodesResult.getOrNull() ?: emptyList()
                    // Store unsorted nodes - sorting will be applied in observeFilters based on current sort order
                    _nodes.value = nodes
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
            combine(_searchQuery, _nodes, _sortOrder) { query, nodes, sortOrder ->
                val filtered = filterNodes(nodes, query)
                sortNodes(filtered, sortOrder)
            }.collect { sorted ->
                _filteredNodes.value = sorted
            }
        }
    }

    private fun filterNodes(nodes: List<SpaceNode>, query: String): List<SpaceNode> {
        return if (query.isBlank()) {
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
    }

    private fun sortNodes(nodes: List<SpaceNode>, sortOrder: NodeSortOrder): List<SpaceNode> {
        return when (sortOrder) {
            NodeSortOrder.DATE_ASC -> {
                // Sort by created_at ascending (oldest first)
                nodes.sortedBy { it.createdAt ?: "" }
            }
            NodeSortOrder.DATE_DESC -> {
                // Sort by created_at descending (newest first)
                nodes.sortedByDescending { it.createdAt ?: "" }
            }
            NodeSortOrder.CONNECTION_ASC -> {
                // Sort by connection count ascending (lowest first)
                nodes.sortedBy { it.connectionCount }
            }
            NodeSortOrder.CONNECTION_DESC -> {
                // Sort by connection count descending (highest first)
                nodes.sortedByDescending { it.connectionCount }
            }
        }
    }
}

