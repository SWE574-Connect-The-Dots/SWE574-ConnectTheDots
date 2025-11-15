package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.network.dto.DeleteEdgeResponse
import com.yybb.myapplication.data.network.dto.UpdateEdgeResponse
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class EdgeDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: EdgeDetailsViewModel
    private lateinit var mockRepository: SpaceNodeDetailsRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val spaceId = "space123"
    private val edgeId = "edge456"
    private val edgeLabel = "Test Edge"
    private val sourceId = "123"
    private val sourceName = "Source Node"
    private val targetId = "456"
    private val targetName = "Target Node"
    private val currentNodeId = "123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        savedStateHandle = SavedStateHandle(
            mapOf(
                "spaceId" to spaceId,
                "edgeId" to edgeId,
                "edgeLabel" to edgeLabel,
                "sourceId" to sourceId,
                "sourceName" to sourceName,
                "targetId" to targetId,
                "targetName" to targetName,
                "currentNodeId" to currentNodeId
            )
        )
    }

    @Test
    fun `initialization should load edge data and nodes successfully`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(123, sourceName, "Q1", null, null, null, null, null, null, null, 0),
            SpaceNode(456, targetName, "Q2", null, null, null, null, null, null, null, 0)
        )
        val mockProperty = WikidataProperty("P1", edgeLabel, "Description", "url")
        whenever(mockRepository.getSpaceNodes(spaceId)).thenReturn(Result.success(mockNodes))
        whenever(mockRepository.searchWikidataEdgeLabels(edgeLabel))
            .thenReturn(Result.success(listOf(mockProperty)))

        // When
        viewModel = EdgeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertEquals(edgeLabel, viewModel.edgeLabel.value)
        assertEquals(spaceId, viewModel.spaceIdValue)
        assertEquals(edgeId, viewModel.edgeIdValue)
        assertEquals(sourceId, viewModel.sourceIdValue)
        assertEquals(targetId, viewModel.targetIdValue)
        assertFalse(viewModel.isLoadingNodes.value)
        assertNull(viewModel.nodesLoadError.value)
    }

    @Test
    fun `initialization should handle missing edge label`() = runTest {
        // Given
        val handleWithoutLabel = SavedStateHandle(
            mapOf(
                "spaceId" to spaceId,
                "edgeId" to edgeId,
                "sourceId" to sourceId,
                "sourceName" to sourceName,
                "targetId" to targetId,
                "targetName" to targetName,
                "currentNodeId" to currentNodeId
            )
        )
        whenever(mockRepository.getSpaceNodes(spaceId)).thenReturn(Result.success(emptyList()))

        // When
        viewModel = EdgeDetailsViewModel(mockRepository, handleWithoutLabel)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.edgeLabel.value.isBlank())
    }

    @Test
    fun `getSourceNodeDisplayText should return formatted text with wikidata id`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(123, sourceName, "Q1", null, null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId)).thenReturn(Result.success(mockNodes))

        viewModel = EdgeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        val displayText = viewModel.getSourceNodeDisplayText()

        // Then
        assertTrue(displayText.contains(sourceName))
        assertTrue(displayText.contains("Q1"))
    }

    @Test
    fun `getSourceNodeDisplayText should return fallback when node not found`() = runTest {
        // Given
        whenever(mockRepository.getSpaceNodes(spaceId)).thenReturn(Result.success(emptyList()))

        viewModel = EdgeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        val displayText = viewModel.getSourceNodeDisplayText()

        // Then
        assertTrue(displayText.contains(sourceName))
        assertTrue(displayText.contains(sourceId))
    }

    @Test
    fun `getTargetNodeDisplayText should return formatted text with wikidata id`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(456, targetName, "Q2", null, null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId)).thenReturn(Result.success(mockNodes))

        viewModel = EdgeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        val displayText = viewModel.getTargetNodeDisplayText()

        // Then
        assertTrue(displayText.contains(targetName))
        assertTrue(displayText.contains("Q2"))
    }

    @Test
    fun `getTargetNodeDisplayText should return fallback when node not found`() = runTest {
        // Given
        whenever(mockRepository.getSpaceNodes(spaceId)).thenReturn(Result.success(emptyList()))

        viewModel = EdgeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        val displayText = viewModel.getTargetNodeDisplayText()

        // Then
        assertTrue(displayText.contains(targetName))
        assertTrue(displayText.contains(targetId))
    }

    @Test
    fun `searchEdgeLabelOptions should search with query length bigger than 3`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "test"
        val mockResults = listOf(
            WikidataProperty("P1", "Property 1", "Description 1", "url1")
        )
        whenever(mockRepository.searchWikidataEdgeLabels(query))
            .thenReturn(Result.success(mockResults))

        // When
        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertEquals(1, viewModel.edgeLabelSearchResults.value.size)
        assertNull(viewModel.edgeLabelSearchError.value)
    }

    @Test
    fun `searchEdgeLabelOptions should not search with query length less than 3 when not initial load`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "te"

        // When
        viewModel.searchEdgeLabelOptions(query, isInitialLoad = false)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
    }

    @Test
    fun `searchEdgeLabelOptions should search on initial load even with short query`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "te"
        val mockResults = listOf(
            WikidataProperty("P1", "Property 1", "Description 1", "url1")
        )
        whenever(mockRepository.searchWikidataEdgeLabels(query))
            .thenReturn(Result.success(mockResults))

        // When
        viewModel.searchEdgeLabelOptions(query, isInitialLoad = true)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertEquals(1, viewModel.edgeLabelSearchResults.value.size)
    }

    @Test
    fun `searchEdgeLabelOptions should handle error on search failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "test"
        val errorMessage = "Search failed"
        whenever(mockRepository.searchWikidataEdgeLabels(query))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
        assertEquals(errorMessage, viewModel.edgeLabelSearchError.value)
    }

    @Test
    fun `selectProperty should set selected property and update edge label`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = WikidataProperty("P1", "New Label", "Description", "url")

        // When
        viewModel.selectProperty(property)
        advanceUntilIdle()

        // Then
        assertEquals(property, viewModel.selectedProperty.value)
        assertEquals("New Label", viewModel.edgeLabel.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
    }

    @Test
    fun `updateEdgeLabel should update label and clear selected property`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = WikidataProperty("P1", "Property", "Description", "url")
        viewModel.selectProperty(property)
        advanceUntilIdle()

        // When
        viewModel.updateEdgeLabel("Updated Label")
        advanceUntilIdle()

        // Then
        assertEquals("Updated Label", viewModel.edgeLabel.value)
        assertNull(viewModel.selectedProperty.value)
    }

    @Test
    fun `resetEdgeLabelSearch should clear search results`() = runTest {
        // Given
        setupViewModelWithMocks()
        viewModel.searchEdgeLabelOptions("test")
        advanceUntilIdle()

        // When
        viewModel.resetEdgeLabelSearch()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
        assertNull(viewModel.edgeLabelSearchError.value)
    }

    @Test
    fun `clearEdgeLabelSearchError should clear error`() = runTest {
        // Given
        setupViewModelWithMocks()
        viewModel.searchEdgeLabelOptions("test")
        advanceUntilIdle()

        // When
        viewModel.clearEdgeLabelSearchError()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.edgeLabelSearchError.value)
    }

    @Test
    fun `updateEdge should update edge successfully in forward direction`() = runTest {
        // Given
        setupViewModelWithMocks()
        val newLabel = "Updated Edge Label"
        val updateResponse = UpdateEdgeResponse("Edge updated")
        whenever(mockRepository.updateEdgeDetails(spaceId, edgeId, newLabel, sourceId, targetId, ""))
            .thenReturn(Result.success(updateResponse))

        // When
        viewModel.updateEdge(newLabel, true)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isUpdatingEdge.value)
        assertTrue(viewModel.updateEdgeSuccess.value)
        assertNull(viewModel.updateEdgeError.value)
        assertEquals(newLabel, viewModel.edgeLabel.value)
        verify(mockRepository).updateEdgeDetails(spaceId, edgeId, newLabel, sourceId, targetId, "")
    }

    @Test
    fun `updateEdge should update edge successfully in reverse direction`() = runTest {
        // Given
        setupViewModelWithMocks()
        val newLabel = "Updated Edge Label"
        val updateResponse = UpdateEdgeResponse("Edge updated")
        whenever(mockRepository.updateEdgeDetails(spaceId, edgeId, newLabel, targetId, sourceId, ""))
            .thenReturn(Result.success(updateResponse))

        // When
        viewModel.updateEdge(newLabel, false)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isUpdatingEdge.value)
        assertTrue(viewModel.updateEdgeSuccess.value)
        verify(mockRepository).updateEdgeDetails(spaceId, edgeId, newLabel, targetId, sourceId, "")
    }

    @Test
    fun `updateEdge should use selected property wikidata id`() = runTest {
        // Given
        setupViewModelWithMocks()
        val property = WikidataProperty("P1", "Property", "Description", "url")
        viewModel.selectProperty(property)
        advanceUntilIdle()

        val newLabel = "Updated Edge Label"
        val updateResponse = UpdateEdgeResponse("Edge updated")
        whenever(mockRepository.updateEdgeDetails(spaceId, edgeId, newLabel, sourceId, targetId, "P1"))
            .thenReturn(Result.success(updateResponse))

        // When
        viewModel.updateEdge(newLabel, true)
        advanceUntilIdle()

        // Then
        verify(mockRepository).updateEdgeDetails(spaceId, edgeId, newLabel, sourceId, targetId, "P1")
    }

    @Test
    fun `updateEdge should handle error on update failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val newLabel = "Updated Edge Label"
        val errorMessage = "Update failed"
        whenever(mockRepository.updateEdgeDetails(spaceId, edgeId, newLabel, sourceId, targetId, ""))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.updateEdge(newLabel, true)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isUpdatingEdge.value)
        assertFalse(viewModel.updateEdgeSuccess.value)
        assertEquals(errorMessage, viewModel.updateEdgeError.value)
    }

    @Test
    fun `clearUpdateEdgeError should clear error`() = runTest {
        // Given
        setupViewModelWithMocks()
        viewModel.updateEdge("label", true)
        advanceUntilIdle()

        // When
        viewModel.clearUpdateEdgeError()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.updateEdgeError.value)
    }

    @Test
    fun `resetUpdateEdgeSuccess should reset success flag`() = runTest {
        // Given
        setupViewModelWithMocks()
        val updateResponse = UpdateEdgeResponse("Edge updated")
        whenever(mockRepository.updateEdgeDetails(eq(spaceId), eq(edgeId), any(), any(), any(), any()))
            .thenReturn(Result.success(updateResponse))

        viewModel.updateEdge("label", true)
        advanceUntilIdle()

        // When
        viewModel.resetUpdateEdgeSuccess()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.updateEdgeSuccess.value)
    }

    @Test
    fun `deleteEdge should delete edge successfully`() = runTest {
        // Given
        setupViewModelWithMocks()
        val deleteResponse = DeleteEdgeResponse("Edge deleted")
        whenever(mockRepository.deleteEdge(spaceId, edgeId))
            .thenReturn(Result.success(deleteResponse))

        // When
        viewModel.deleteEdge()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isDeletingEdge.value)
        assertTrue(viewModel.deleteEdgeSuccess.value)
        assertNull(viewModel.deleteEdgeError.value)
        verify(mockRepository).deleteEdge(spaceId, edgeId)
    }

    @Test
    fun `deleteEdge should handle error on deletion failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val errorMessage = "Deletion failed"
        whenever(mockRepository.deleteEdge(spaceId, edgeId))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.deleteEdge()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isDeletingEdge.value)
        assertFalse(viewModel.deleteEdgeSuccess.value)
        assertEquals(errorMessage, viewModel.deleteEdgeError.value)
    }

    @Test
    fun `clearDeleteEdgeError should clear error`() = runTest {
        // Given
        setupViewModelWithMocks()
        viewModel.deleteEdge()
        advanceUntilIdle()

        // When
        viewModel.clearDeleteEdgeError()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.deleteEdgeError.value)
    }

    @Test
    fun `resetDeleteEdgeSuccess should reset success flag`() = runTest {
        // Given
        setupViewModelWithMocks()
        val deleteResponse = DeleteEdgeResponse("Edge deleted")
        whenever(mockRepository.deleteEdge(spaceId, edgeId))
            .thenReturn(Result.success(deleteResponse))

        viewModel.deleteEdge()
        advanceUntilIdle()

        // When
        viewModel.resetDeleteEdgeSuccess()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.deleteEdgeSuccess.value)
    }

    @Test
    fun `isCurrentNodeSource should return true when current node is source`() = runTest {
        // Given
        setupViewModelWithMocks()

        // When
        val isSource = viewModel.isCurrentNodeSource()

        // Then
        assertTrue(isSource) // currentNodeId is "123" which equals sourceId
    }

    @Test
    fun `getOtherNodeId should return target when current is source`() = runTest {
        // Given
        setupViewModelWithMocks()

        // When
        val otherNodeId = viewModel.getOtherNodeId()

        // Then
        assertEquals(targetId, otherNodeId)
    }

    @Test
    fun `getOtherNodeLabel should return target label when current is source`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(456, targetName, "Q2", null, null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        viewModel = EdgeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        val otherNodeLabel = viewModel.getOtherNodeLabel()

        // Then
        assertEquals(targetName, otherNodeLabel)
    }

    @Test
    fun `getOtherNodeWikidataId should return target wikidata id when current is source`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(456, targetName, "Q2", null, null, null, null, null, null, null, 0)
        )
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        viewModel = EdgeDetailsViewModel(mockRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        val otherNodeWikidataId = viewModel.getOtherNodeWikidataId()

        // Then
        assertEquals("Q2", otherNodeWikidataId)
    }

    // Helper functions
    private suspend fun setupViewModelWithMocks() {
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(emptyList()))
        viewModel = EdgeDetailsViewModel(mockRepository, savedStateHandle)
    }
}

