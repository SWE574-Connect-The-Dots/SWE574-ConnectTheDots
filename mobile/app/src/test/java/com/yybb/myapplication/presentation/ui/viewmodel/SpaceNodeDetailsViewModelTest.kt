package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.SpaceEdge
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.network.dto.AddEdgeResponse
import com.yybb.myapplication.data.network.dto.CreateSnapshotResponse
import com.yybb.myapplication.data.network.dto.DeleteNodeResponse
import com.yybb.myapplication.data.repository.SpaceNodeDetailsRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SpaceNodeDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SpaceNodeDetailsViewModel
    private lateinit var mockRepository: SpaceNodeDetailsRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val spaceId = "space123"
    private val nodeId = "node456"
    private val nodeLabel = "Test Node"
    private val wikidataId = "Q123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        savedStateHandle = SavedStateHandle(
            mapOf(
                "spaceId" to spaceId,
                "nodeId" to nodeId,
                "nodeLabel" to nodeLabel,
                "nodeWikidataId" to wikidataId
            )
        )
    }

    @Test
    fun `initialization with blank node label should set critical error`() = runTest {
        // Given
        val handleWithoutLabel = SavedStateHandle(
            mapOf(
                "spaceId" to spaceId,
                "nodeId" to nodeId
            )
        )

        // When
        viewModel = SpaceNodeDetailsViewModel(mockRepository, handleWithoutLabel)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.criticalError.value)
        assertTrue(viewModel.criticalError.value!!.contains("Node name is required"))
    }

    @Test
    fun `clearCriticalError should reset critical error`() = runTest {
        // Given
        val handleWithoutLabel = SavedStateHandle(
            mapOf(
                "spaceId" to spaceId,
                "nodeId" to nodeId
            )
        )
        viewModel = SpaceNodeDetailsViewModel(mockRepository, handleWithoutLabel)
        advanceUntilIdle()

        // When
        viewModel.clearCriticalError()

        // Then
        assertNull(viewModel.criticalError.value)
    }

    @Test
    fun `updateSearchQuery should update search query`() = runTest {
        // Given
        setupViewModelWithMocks()
        initializeViewModel()
        val query = "test query"

        // When
        viewModel.updateSearchQuery(query)
        advanceUntilIdle()

        // Then
        assertEquals(query, viewModel.searchQuery.value)
    }

    @Test
    fun `updateConnectionSearchQuery should update connection search query`() = runTest {
        // Given
        setupViewModelWithMocks()
        initializeViewModel()
        val query = "connection query"

        // When
        viewModel.updateConnectionSearchQuery(query)
        advanceUntilIdle()

        // Then
        assertEquals(query, viewModel.connectionSearchQuery.value)
    }

    @Test
    fun `resetConnectionSearchQuery should clear connection search query`() = runTest {
        // Given
        setupViewModelWithMocks()
        initializeViewModel()
        viewModel.updateConnectionSearchQuery("test")
        advanceUntilIdle()

        // When
        viewModel.resetConnectionSearchQuery()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.connectionSearchQuery.value.isBlank())
    }

    @Test
    fun `togglePropertySelection should toggle property checked state`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        viewModel = SpaceNodeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        val initialChecked = viewModel.propertyOptions.value.firstOrNull()?.isChecked ?: false

        // When
        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        // Then
        val afterToggle = viewModel.propertyOptions.value.firstOrNull()?.isChecked ?: false
        assertTrue(initialChecked != afterToggle)
    }

    @Test
    fun `saveSelectedProperties should update node properties successfully`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.updateNodeProperties(eq(spaceId), eq(nodeId), any()))
            .thenReturn(Result.success(Unit))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        // When
        viewModel.saveSelectedProperties()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isUpdatingNodeProperties.value)
        assertNull(viewModel.nodePropertiesError.value)
        verify(mockRepository).updateNodeProperties(eq(spaceId), eq(nodeId), any())
    }

    @Test
    fun `saveSelectedProperties should handle error on update failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val errorMessage = "Update failed"
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.updateNodeProperties(eq(spaceId), eq(nodeId), any()))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        // When
        viewModel.saveSelectedProperties()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isUpdatingNodeProperties.value)
        assertEquals(errorMessage, viewModel.nodePropertiesError.value)
    }

    @Test
    fun `retryNodeProperties should refetch node and wikidata properties`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // Clear invocations from initialization
        clearInvocations(mockRepository)

        // When
        viewModel.retryNodeProperties()
        advanceUntilIdle()

        // Then
        verify(mockRepository).getNodeProperties(spaceId, nodeId)
        verify(mockRepository).getWikidataEntityProperties(wikidataId)
    }

    @Test
    fun `deleteNodeProperty should delete property successfully`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.deleteNodeProperty(spaceId, nodeId, "prop1"))
            .thenReturn(Result.success(Unit))
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.deleteNodeProperty(property)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isDeletingNodeProperty.value)
        assertNotNull(viewModel.nodePropertyDeletionMessage.value)
        assertNull(viewModel.nodePropertyDeletionError.value)
    }

    @Test
    fun `deleteNodeProperty should handle error on deletion failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val errorMessage = "Deletion failed"
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.deleteNodeProperty(spaceId, nodeId, "prop1"))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.deleteNodeProperty(property)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isDeletingNodeProperty.value)
        assertEquals(errorMessage, viewModel.nodePropertyDeletionError.value)
    }

    @Test
    fun `searchEdgeLabelOptions should search with query length bigger than or equal to 3`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "test"
        val mockResults = listOf(
            WikidataProperty("P1", "Property 1", "Description 1", "url1")
        )
        whenever(mockRepository.searchWikidataEdgeLabels(query))
            .thenReturn(Result.success(mockResults))
        initializeViewModel()

        // When
        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isNotEmpty())
    }

    @Test
    fun `searchEdgeLabelOptions should not search with query length less than 3`() = runTest {
        // Given
        setupViewModelWithMocks()
        initializeViewModel()
        val query = "te"

        // When
        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
    }

    @Test
    fun `resetEdgeLabelSearch should clear search results`() = runTest {
        // Given
        setupViewModelWithMocks()
        initializeViewModel()

        // When
        viewModel.resetEdgeLabelSearch()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
        assertNull(viewModel.edgeLabelSearchError.value)
    }

    @Test
    fun `addEdge should create edge successfully`() = runTest {
        // Given
        setupViewModelWithMocks()
        val nodeOption = SpaceNodeDetailsViewModel.NodeOption("node789", "Connected Node")
        val addEdgeResponse = AddEdgeResponse("Edge created", 1)
        val snapshotResponse = CreateSnapshotResponse(1, "2024-01-01")
        val mockEdges = listOf(
            SpaceEdge(1, 456, 789, "New Edge", null)
        )

        whenever(mockRepository.addEdgeToSpaceGraph(spaceId, nodeId, "node789", "New Edge", ""))
            .thenReturn(Result.success(addEdgeResponse))
        whenever(mockRepository.createSnapshot(spaceId))
            .thenReturn(Result.success(snapshotResponse))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(mockEdges))
        initializeViewModel()

        // When
        viewModel.addEdge(nodeOption, true, "New Edge", "")
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isCreatingEdge.value)
        assertNotNull(viewModel.edgeCreationSuccess.value)
        assertEquals(1, viewModel.edgeCreationSuccess.value!!.edgeId)
    }

    @Test
    fun `addEdge should handle error on edge creation failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val nodeOption = SpaceNodeDetailsViewModel.NodeOption("node789", "Connected Node")
        val errorMessage = "Edge creation failed"
        whenever(mockRepository.addEdgeToSpaceGraph(spaceId, nodeId, "node789", "New Edge", ""))
            .thenReturn(Result.failure(Exception(errorMessage)))
        initializeViewModel()

        // When
        viewModel.addEdge(nodeOption, true, "New Edge", "")
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isCreatingEdge.value)
        assertEquals(errorMessage, viewModel.edgeCreationError.value)
    }

    @Test
    fun `deleteNode should delete node successfully`() = runTest {
        // Given
        setupViewModelWithMocks()
        val deleteResponse = DeleteNodeResponse("Node deleted")
        val snapshotResponse = CreateSnapshotResponse(1, "2024-01-01")

        whenever(mockRepository.deleteNode(spaceId, nodeId))
            .thenReturn(Result.success(deleteResponse))
        whenever(mockRepository.createSnapshot(spaceId))
            .thenReturn(Result.success(snapshotResponse))
        initializeViewModel()

        // When
        viewModel.deleteNode()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isDeletingNode.value)
        assertTrue(viewModel.deleteNodeSuccess.value)
        assertNull(viewModel.deleteNodeError.value)
    }

    @Test
    fun `deleteNode should handle error on deletion failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val errorMessage = "Node deletion failed"
        whenever(mockRepository.deleteNode(spaceId, nodeId))
            .thenReturn(Result.failure(Exception(errorMessage)))
        initializeViewModel()

        // When
        viewModel.deleteNode()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isDeletingNode.value)
        assertEquals(errorMessage, viewModel.deleteNodeError.value)
        assertFalse(viewModel.deleteNodeSuccess.value)
    }

    @Test
    fun `refreshNodeConnections should refetch node connections`() = runTest {
        // Given
        setupViewModelWithMocks()
        val mockEdges = listOf(
            SpaceEdge(1, 456, 1, "Edge 1", null)
        )
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(mockEdges))
        initializeViewModel()

        // When
        viewModel.refreshNodeConnections()
        advanceUntilIdle()

        // Then
        verify(mockRepository).getSpaceEdges(spaceId)
    }

    @Test
    fun `filteredConnections should filter by search query`() = runTest {
        // Given
        setupViewModelWithMocks()
        val mockEdges = listOf(
            SpaceEdge(1, 456, 1, "Test Edge", null),
            SpaceEdge(2, 1, 456, "Other Edge", null)
        )
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(mockEdges))
        initializeViewModel()

        // When
        viewModel.updateConnectionSearchQuery("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredConnections.value
        assertTrue(filtered.all { it.label.contains("Test", ignoreCase = true) })
    }

    @Test
    fun `filteredOptions should filter properties by search query`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property1 = createMockNodeProperty("prop1", "Test Property", "Value 1")
        val property2 = createMockNodeProperty("prop2", "Other Property", "Value 2")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property1, property2)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property1, property2)))
        initializeViewModel()

        // When
        viewModel.updateSearchQuery("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredOptions.value
        assertTrue(filtered.all { it.property.display.contains("Test", ignoreCase = true) })
    }

    // Helper functions
    private suspend fun setupViewModelWithMocks() {
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))
    }

    private suspend fun initializeViewModel() {
        viewModel = SpaceNodeDetailsViewModel(mockRepository, savedStateHandle)
    }

    private fun createMockNodeProperty(
        statementId: String,
        propertyLabel: String,
        valueText: String
    ): NodeProperty {
        return NodeProperty(
            statementId = statementId,
            propertyId = "P$statementId",
            propertyLabel = propertyLabel,
            valueText = valueText,
            isEntity = false,
            entityId = null,
            display = "$propertyLabel: $valueText"
        )
    }
}

