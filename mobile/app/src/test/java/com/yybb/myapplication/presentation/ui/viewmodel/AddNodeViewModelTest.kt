package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.network.dto.AddNodeResponse
import com.yybb.myapplication.data.network.dto.CreateSnapshotResponse
import com.yybb.myapplication.data.repository.SpaceNodeDetailsRepository
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AddNodeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AddNodeViewModel
    private lateinit var mockNodeDetailsRepository: SpaceNodeDetailsRepository
    private lateinit var mockNodesRepository: SpaceNodesRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val spaceId = "space123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockNodeDetailsRepository = mock()
        mockNodesRepository = mock()
        savedStateHandle = SavedStateHandle(mapOf("spaceId" to spaceId))
    }

    @Test
    fun `initialization should load space nodes`() = runTest {
        // Given
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, null, null, null, null, null, 0),
            SpaceNode(2, "Node 2", null, null, null, null, null, null, null, null, 0)
        )
        whenever(mockNodesRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))

        // When
        viewModel = AddNodeViewModel(mockNodeDetailsRepository, mockNodesRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoadingNodes.value)
        assertEquals(2, viewModel.availableNodes.value.size)
    }

    @Test
    fun `searchWikidataEntities should search entities successfully`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "test entity"
        val mockResults = listOf(
            WikidataProperty("Q1", "Entity 1", "Description 1", "url1"),
            WikidataProperty("Q2", "Entity 2", "Description 2", "url2")
        )
        whenever(mockNodeDetailsRepository.searchWikidataEntities(query))
            .thenReturn(Result.success(mockResults))

        // When
        viewModel.searchWikidataEntities(query)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isSearchingWikidata.value)
        assertEquals(2, viewModel.wikidataSearchResults.value.size)
        assertNull(viewModel.wikidataSearchError.value)
    }

    @Test
    fun `searchWikidataEntities should handle error on search failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "test entity"
        val errorMessage = "Search failed"
        whenever(mockNodeDetailsRepository.searchWikidataEntities(query))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.searchWikidataEntities(query)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isSearchingWikidata.value)
        assertTrue(viewModel.wikidataSearchResults.value.isEmpty())
        assertEquals(errorMessage, viewModel.wikidataSearchError.value)
    }

    @Test
    fun `selectEntity should set selected entity and load properties`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val mockProperties = listOf(
            createMockNodeProperty("prop1", "Property 1", "Value 1")
        )
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(mockProperties))

        // When
        viewModel.selectEntity(entity)
        advanceUntilIdle()

        // Then
        assertEquals(entity, viewModel.selectedEntity.value)
        assertTrue(viewModel.wikidataSearchResults.value.isEmpty())
        assertEquals(1, viewModel.entityProperties.value.size)
        assertFalse(viewModel.isLoadingProperties.value)
    }

    @Test
    fun `selectEntity should handle error on properties load failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val errorMessage = "Properties load failed"
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // When
        viewModel.selectEntity(entity)
        advanceUntilIdle()

        // Then
        assertEquals(entity, viewModel.selectedEntity.value)
        assertTrue(viewModel.entityProperties.value.isEmpty())
        assertEquals(errorMessage, viewModel.propertiesError.value)
    }

    @Test
    fun `updatePropertySearchQuery should update search query`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "test query"

        // When
        viewModel.updatePropertySearchQuery(query)
        advanceUntilIdle()

        // Then
        assertEquals(query, viewModel.propertySearchQuery.value)
    }

    @Test
    fun `togglePropertySelection should add property to selected`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        // When
        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.selectedProperties.value.contains("prop1"))
    }

    @Test
    fun `togglePropertySelection should remove property from selected when already selected`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()
        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        // When
        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.selectedProperties.value.contains("prop1"))
    }

    @Test
    fun `searchEdgeLabelOptions should search with query length bigger than 2`() = runTest {
        // Given
        setupViewModelWithMocks()
        val query = "test"
        val mockResults = listOf(
            WikidataProperty("P1", "Property 1", "Description 1", "url1")
        )
        whenever(mockNodeDetailsRepository.searchWikidataProperties(query))
            .thenReturn(Result.success(mockResults))

        // When
        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertEquals(1, viewModel.edgeLabelSearchResults.value.size)
    }

    @Test
    fun `searchEdgeLabelOptions should not search with query length less than or equal 2`() = runTest {
        // Given
        setupViewModelWithMocks()
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

        // When
        viewModel.resetEdgeLabelSearch()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
        assertNull(viewModel.edgeLabelSearchError.value)
    }

    @Test
    fun `createNode should fail when no entity is selected`() = runTest {
        // Given
        setupViewModelWithMocks()

        // When
        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.createNodeError.value)
        assertTrue(viewModel.createNodeError.value!!.contains("select an entity"))
    }

    @Test
    fun `createNode should fail when relatedNodeId is provided but edgeLabel is blank`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        // When
        viewModel.createNode("node123", "", true, null)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.createNodeError.value)
        assertTrue(viewModel.createNodeError.value!!.contains("edge label"))
    }

    @Test
    fun `createNode should create node successfully without connection`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val addNodeResponse = AddNodeResponse("Node created successfully")
        val snapshotResponse = CreateSnapshotResponse(1, "2024-01-01")

        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockNodeDetailsRepository.addNode(eq(spaceId), any()))
            .thenReturn(Result.success(addNodeResponse))
        whenever(mockNodeDetailsRepository.createSnapshot(spaceId))
            .thenReturn(Result.success(snapshotResponse))

        viewModel.selectEntity(entity)
        advanceUntilIdle()
        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        // When
        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isCreatingNode.value)
        assertNotNull(viewModel.createNodeSuccess.value)
        assertEquals("Node created successfully", viewModel.createNodeSuccess.value)
    }

    @Test
    fun `createNode should create node with connection successfully`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val edgeProperty = WikidataProperty("P1", "Property 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val addNodeResponse = AddNodeResponse("Node created successfully")
        val snapshotResponse = CreateSnapshotResponse(1, "2024-01-01")

        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockNodeDetailsRepository.addNode(eq(spaceId), any()))
            .thenReturn(Result.success(addNodeResponse))
        whenever(mockNodeDetailsRepository.createSnapshot(spaceId))
            .thenReturn(Result.success(snapshotResponse))

        viewModel.selectEntity(entity)
        advanceUntilIdle()
        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        // When
        viewModel.createNode("node123", "connected to", true, edgeProperty)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isCreatingNode.value)
        assertNotNull(viewModel.createNodeSuccess.value)
        verify(mockNodeDetailsRepository).addNode(eq(spaceId), any())
    }

    @Test
    fun `createNode should handle error on node creation failure`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val errorMessage = "Node creation failed"
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockNodeDetailsRepository.addNode(eq(spaceId), any()))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        // When
        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isCreatingNode.value)
        assertEquals(errorMessage, viewModel.createNodeError.value)
    }

    @Test
    fun `clearWikidataSearchError should clear error`() = runTest {
        // Given
        setupViewModelWithMocks()
        viewModel.searchWikidataEntities("test")
        advanceUntilIdle()

        // When
        viewModel.clearWikidataSearchError()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.wikidataSearchError.value)
    }

    @Test
    fun `clearCreateNodeError should clear error`() = runTest {
        // Given
        setupViewModelWithMocks()
        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        // When
        viewModel.clearCreateNodeError()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.createNodeError.value)
    }

    @Test
    fun `clearCreateNodeSuccess should clear success message`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val addNodeResponse = AddNodeResponse("Node created successfully")
        val snapshotResponse = CreateSnapshotResponse(1, "2024-01-01")

        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockNodeDetailsRepository.addNode(eq(spaceId), any()))
            .thenReturn(Result.success(addNodeResponse))
        whenever(mockNodeDetailsRepository.createSnapshot(spaceId))
            .thenReturn(Result.success(snapshotResponse))

        viewModel.selectEntity(entity)
        advanceUntilIdle()
        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        // When
        viewModel.clearCreateNodeSuccess()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.createNodeSuccess.value)
    }

    @Test
    fun `filteredProperties should filter by search query`() = runTest {
        // Given
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property1 = createMockNodeProperty("prop1", "Test Property", "Value 1")
        val property2 = createMockNodeProperty("prop2", "Other Property", "Value 2")
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property1, property2)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        // When
        viewModel.updatePropertySearchQuery("Test")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredProperties.value
        assertTrue(filtered.all { 
            it.propertyLabel.contains("Test", ignoreCase = true) || 
            it.valueText.contains("Test", ignoreCase = true) 
        })
    }

    // Helper functions
    private suspend fun setupViewModelWithMocks() {
        whenever(mockNodesRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(emptyList()))
        viewModel = AddNodeViewModel(mockNodeDetailsRepository, mockNodesRepository, savedStateHandle)
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

