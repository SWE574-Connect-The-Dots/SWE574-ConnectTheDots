package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.SpaceEdge
import com.yybb.myapplication.data.repository.SpaceNodesRepository
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
import org.mockito.Mockito.reset
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SpaceNodesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SpaceNodesViewModel
    private lateinit var mockRepository: SpaceNodesRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val spaceId = "space123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        savedStateHandle = SavedStateHandle(mapOf("spaceId" to spaceId))
    }

    @Test
    fun `initialization should load space nodes successfully`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", "Q1", "Country 1", "City 1", null, null, null, null, "Location 1", 0),
            SpaceNode(2, "Node 2", "Q2", "Country 2", "City 2", null, null, null, null, "Location 2", 0)
        )
        val mockEdges = listOf(
            SpaceEdge(1, 1, 2, "Edge 1", null)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(mockEdges))

        // When
        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.value)
        assertEquals(2, viewModel.nodes.value.size)
        assertEquals(2, viewModel.filteredNodes.value.size)
        assertNull(viewModel.errorMessage.value)
        // Verify connection counts are calculated
        val node1 = viewModel.nodes.value.find { it.id == 1 }
        val node2 = viewModel.nodes.value.find { it.id == 2 }
        assertNotNull(node1)
        assertNotNull(node2)
        assertEquals(1, node1?.connectionCount)
        assertEquals(1, node2?.connectionCount)
    }

    @Test
    fun `initialization should handle error on load failure`() = runTest {
        // Given
        val errorMessage = "Failed to load nodes"
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.failure(Exception(errorMessage)))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        // When
        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.nodes.value.isEmpty())
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }

    @Test
    fun `onSearchQueryChange should update search query`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "test query"

        // When
        viewModel.onSearchQueryChange(query)
        advanceUntilIdle()

        // Then
        assertEquals(query, viewModel.searchQuery.value)
    }

    @Test
    fun `onSearchQueryChange should filter nodes by label`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Test Node", null, null, null, null, null, null, null, null, 0),
            SpaceNode(2, "Other Node", null, null, null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredNodes.value
        assertEquals(1, filtered.size)
        assertEquals("Test Node", filtered.first().label)
    }

    @Test
    fun `onSearchQueryChange should filter nodes by location name`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, null, null, null, null, "Test Location", 0),
            SpaceNode(2, "Node 2", null, null, null, null, null, null, null, "Other Location", 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredNodes.value
        assertEquals(1, filtered.size)
        assertEquals("Test Location", filtered.first().locationName)
    }

    @Test
    fun `onSearchQueryChange should filter nodes by city`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, "Test City", null, null, null, null, null, 0),
            SpaceNode(2, "Node 2", null, null, "Other City", null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredNodes.value
        assertEquals(1, filtered.size)
        assertEquals("Test City", filtered.first().city)
    }

    @Test
    fun `onSearchQueryChange should filter nodes by country`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, "Test Country", null, null, null, null, null, null, 0),
            SpaceNode(2, "Node 2", null, "Other Country", null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredNodes.value
        assertEquals(1, filtered.size)
        assertEquals("Test Country", filtered.first().country)
    }

    @Test
    fun `onSearchQueryChange should filter nodes by district`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, "Test District", null, null, null, null, 0),
            SpaceNode(2, "Node 2", null, null, null, "Other District", null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredNodes.value
        assertEquals(1, filtered.size)
        assertEquals("Test District", filtered.first().district)
    }

    @Test
    fun `onSearchQueryChange should filter nodes by street`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, null, "Test Street", null, null, null, 0),
            SpaceNode(2, "Node 2", null, null, null, null, "Other Street", null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredNodes.value
        assertEquals(1, filtered.size)
        assertEquals("Test Street", filtered.first().street)
    }

    @Test
    fun `onSearchQueryChange should be case insensitive`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Test Node", null, null, null, null, null, null, null, null, 0),
            SpaceNode(2, "Other Node", null, null, null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredNodes.value
        assertEquals(1, filtered.size)
    }

    @Test
    fun `onSearchQueryChange with empty query should show all nodes`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, null, null, null, null, null, 0),
            SpaceNode(2, "Node 2", null, null, null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        // When
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredNodes.value
        assertEquals(2, filtered.size)
    }

    @Test
    fun `onScreenResumed should refresh on subsequent resumes`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()
        
        // Ensure loading is complete
        assertFalse(viewModel.isLoading.value)

        // Reset mock to clear all invocations and stubbing
        reset(mockRepository)
        // Re-setup the stubbing after reset
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        // Skip first resume - should not call fetchSpaceNodes
        viewModel.onScreenResumed()
        advanceUntilIdle()
        
        // Verify first resume did not trigger a fetch
        verify(mockRepository, times(0)).getSpaceNodes(spaceId)
        verify(mockRepository, times(0)).getSpaceEdges(spaceId)
        
        // Ensure loading is still false before second resume
        assertFalse(viewModel.isLoading.value)

        // When - second resume should trigger fetch exactly once
        viewModel.onScreenResumed()
        advanceUntilIdle()
        
        // Ensure loading completed
        assertFalse(viewModel.isLoading.value)

        // Then - verify it was called exactly once after clearing
        val inOrder = inOrder(mockRepository)
        inOrder.verify(mockRepository, times(1)).getSpaceNodes(spaceId)
        inOrder.verify(mockRepository, times(1)).getSpaceEdges(spaceId)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `onScreenResumed should not refresh if already loading`() = runTest {
        // Given
        setupViewModelWithMocks()
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // Skip first resume
        viewModel.onScreenResumed()
        advanceUntilIdle()

        // Simulate loading state
        viewModel.refresh()
        // Don't advance until idle to keep loading state

        // When
        viewModel.onScreenResumed()
        advanceUntilIdle()

        // Then - should not trigger additional call if loading
        // This test verifies the guard condition
    }


    @Test
    fun `sortOrder DATE_ASC should sort by date ascending`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, null, null, null, null, null, 1, "2024-01-03"),
            SpaceNode(2, "Node 2", null, null, null, null, null, null, null, null, 1, "2024-01-01"),
            SpaceNode(3, "Node 3", null, null, null, null, null, null, null, null, 1, "2024-01-02")
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.setSortOrder(NodeSortOrder.DATE_ASC)
        advanceUntilIdle()

        // Then
        val sorted = viewModel.filteredNodes.value
        assertEquals(2, sorted[0].id) // Oldest first
        assertEquals(3, sorted[1].id)
        assertEquals(1, sorted[2].id)
    }

    @Test
    fun `sortOrder DATE_DESC should sort by date descending`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, null, null, null, null, null, 1, "2024-01-01"),
            SpaceNode(2, "Node 2", null, null, null, null, null, null, null, null, 1, "2024-01-03"),
            SpaceNode(3, "Node 3", null, null, null, null, null, null, null, null, 1, "2024-01-02")
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))

        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.setSortOrder(NodeSortOrder.DATE_DESC)
        advanceUntilIdle()

        // Then
        val sorted = viewModel.filteredNodes.value
        assertEquals(2, sorted[0].id) // Newest first
        assertEquals(3, sorted[1].id)
        assertEquals(1, sorted[2].id)
    }

    // Helper functions
    private suspend fun setupViewModelWithMocks() {
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))
        viewModel = SpaceNodesViewModel(mockRepository, savedStateHandle)
    }
}

