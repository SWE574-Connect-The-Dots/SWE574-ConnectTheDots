package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.SpaceEdge
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.network.dto.AddEdgeResponse
import com.yybb.myapplication.data.network.dto.CountryPosition
import com.yybb.myapplication.data.network.dto.CreateSnapshotResponse
import com.yybb.myapplication.data.network.dto.DeleteNodeResponse
import com.yybb.myapplication.data.network.dto.NodeLocationData
import com.yybb.myapplication.data.network.dto.NominatimCoordinates
import com.yybb.myapplication.data.network.dto.UpdateNodeLocationResponse
import com.yybb.myapplication.data.repository.CountriesRepository
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
    private lateinit var mockSpacesRepository: com.yybb.myapplication.data.repository.SpacesRepository
    private lateinit var mockCountriesRepository: CountriesRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val spaceId = "space123"
    private val nodeId = "node456"
    private val nodeLabel = "Test Node"
    private val wikidataId = "Q123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        mockSpacesRepository = mock()
        mockCountriesRepository = mock()
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
        val handleWithoutLabel = SavedStateHandle(
            mapOf(
                "spaceId" to spaceId,
                "nodeId" to nodeId
            )
        )

        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, handleWithoutLabel)
        advanceUntilIdle()

        assertNotNull(viewModel.criticalError.value)
        assertTrue(viewModel.criticalError.value!!.contains("Node name is required"))
    }

    @Test
    fun `clearCriticalError should reset critical error`() = runTest {
        val handleWithoutLabel = SavedStateHandle(
            mapOf(
                "spaceId" to spaceId,
                "nodeId" to nodeId
            )
        )
        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, handleWithoutLabel)
        advanceUntilIdle()

        viewModel.clearCriticalError()

        assertNull(viewModel.criticalError.value)
    }

    @Test
    fun `updateSearchQuery should update search query`() = runTest {
        setupViewModelWithMocks()
        initializeViewModel()
        val query = "test query"

        viewModel.updateSearchQuery(query)
        advanceUntilIdle()

        assertEquals(query, viewModel.searchQuery.value)
    }

    @Test
    fun `updateConnectionSearchQuery should update connection search query`() = runTest {
        setupViewModelWithMocks()
        initializeViewModel()
        val query = "connection query"

        viewModel.updateConnectionSearchQuery(query)
        advanceUntilIdle()

        assertEquals(query, viewModel.connectionSearchQuery.value)
    }

    @Test
    fun `resetConnectionSearchQuery should clear connection search query`() = runTest {
        setupViewModelWithMocks()
        initializeViewModel()
        viewModel.updateConnectionSearchQuery("test")
        advanceUntilIdle()

        viewModel.resetConnectionSearchQuery()
        advanceUntilIdle()

        assertTrue(viewModel.connectionSearchQuery.value.isBlank())
    }

    @Test
    fun `togglePropertySelection should toggle property checked state`() = runTest {
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()

        val initialChecked = viewModel.propertyOptions.value.firstOrNull()?.isChecked ?: false

        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        val afterToggle = viewModel.propertyOptions.value.firstOrNull()?.isChecked ?: false
        assertTrue(initialChecked != afterToggle)
    }

    @Test
    fun `saveSelectedProperties should update node properties successfully`() = runTest {
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.updateNodeProperties(eq(spaceId), eq(nodeId), any()))
            .thenReturn(Result.success(Unit))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        viewModel.saveSelectedProperties()
        advanceUntilIdle()

        assertFalse(viewModel.isUpdatingNodeProperties.value)
        assertNull(viewModel.nodePropertiesError.value)
        verify(mockRepository).updateNodeProperties(eq(spaceId), eq(nodeId), any())
    }

    @Test
    fun `saveSelectedProperties should handle error on update failure`() = runTest {
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val errorMessage = "Update failed"
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.updateNodeProperties(eq(spaceId), eq(nodeId), any()))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        viewModel.saveSelectedProperties()
        advanceUntilIdle()

        assertFalse(viewModel.isUpdatingNodeProperties.value)
        assertEquals(errorMessage, viewModel.nodePropertiesError.value)
    }

    @Test
    fun `retryNodeProperties should refetch node and wikidata properties`() = runTest {
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()

        // Clear invocations from initialization
        clearInvocations(mockRepository)

        viewModel.retryNodeProperties()
        advanceUntilIdle()

        verify(mockRepository).getNodeProperties(spaceId, nodeId)
        verify(mockRepository).getWikidataEntityProperties(wikidataId)
    }

    @Test
    fun `deleteNodeProperty should delete property successfully`() = runTest {
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

        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.deleteNodeProperty(property)
        advanceUntilIdle()

        assertFalse(viewModel.isDeletingNodeProperty.value)
        assertNotNull(viewModel.nodePropertyDeletionMessage.value)
        assertNull(viewModel.nodePropertyDeletionError.value)
    }

    @Test
    fun `deleteNodeProperty should handle error on deletion failure`() = runTest {
        setupViewModelWithMocks()
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val errorMessage = "Deletion failed"
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockRepository.deleteNodeProperty(spaceId, nodeId, "prop1"))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.deleteNodeProperty(property)
        advanceUntilIdle()

        assertFalse(viewModel.isDeletingNodeProperty.value)
        assertEquals(errorMessage, viewModel.nodePropertyDeletionError.value)
    }

    @Test
    fun `searchEdgeLabelOptions should search with query length bigger than or equal to 3`() = runTest {
        setupViewModelWithMocks()
        val query = "test"
        val mockResults = listOf(
            WikidataProperty("P1", "Property 1", "Description 1", "url1")
        )
        whenever(mockRepository.searchWikidataEdgeLabels(query))
            .thenReturn(Result.success(mockResults))
        initializeViewModel()

        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isNotEmpty())
    }

    @Test
    fun `searchEdgeLabelOptions should not search with query length less than 3`() = runTest {
        setupViewModelWithMocks()
        initializeViewModel()
        val query = "te"

        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
    }

    @Test
    fun `resetEdgeLabelSearch should clear search results`() = runTest {
        setupViewModelWithMocks()
        initializeViewModel()

        viewModel.resetEdgeLabelSearch()
        advanceUntilIdle()

        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
        assertNull(viewModel.edgeLabelSearchError.value)
    }

    @Test
    fun `addEdge should create edge successfully`() = runTest {
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

        viewModel.addEdge(nodeOption, true, "New Edge", "")
        advanceUntilIdle()

        assertFalse(viewModel.isCreatingEdge.value)
        assertNotNull(viewModel.edgeCreationSuccess.value)
        assertEquals(1, viewModel.edgeCreationSuccess.value!!.edgeId)
    }

    @Test
    fun `addEdge should handle error on edge creation failure`() = runTest {
        setupViewModelWithMocks()
        val nodeOption = SpaceNodeDetailsViewModel.NodeOption("node789", "Connected Node")
        val errorMessage = "Edge creation failed"
        whenever(mockRepository.addEdgeToSpaceGraph(spaceId, nodeId, "node789", "New Edge", ""))
            .thenReturn(Result.failure(Exception(errorMessage)))
        initializeViewModel()

        viewModel.addEdge(nodeOption, true, "New Edge", "")
        advanceUntilIdle()

        assertFalse(viewModel.isCreatingEdge.value)
        assertEquals(errorMessage, viewModel.edgeCreationError.value)
    }

    @Test
    fun `deleteNode should delete node successfully`() = runTest {
        setupViewModelWithMocks()
        val deleteResponse = DeleteNodeResponse("Node deleted")
        val snapshotResponse = CreateSnapshotResponse(1, "2024-01-01")

        whenever(mockRepository.deleteNode(spaceId, nodeId))
            .thenReturn(Result.success(deleteResponse))
        whenever(mockRepository.createSnapshot(spaceId))
            .thenReturn(Result.success(snapshotResponse))
        initializeViewModel()

        viewModel.deleteNode()
        advanceUntilIdle()

        assertFalse(viewModel.isDeletingNode.value)
        assertTrue(viewModel.deleteNodeSuccess.value)
        assertNull(viewModel.deleteNodeError.value)
    }

    @Test
    fun `deleteNode should handle error on deletion failure`() = runTest {
        setupViewModelWithMocks()
        val errorMessage = "Node deletion failed"
        whenever(mockRepository.deleteNode(spaceId, nodeId))
            .thenReturn(Result.failure(Exception(errorMessage)))
        initializeViewModel()

        viewModel.deleteNode()
        advanceUntilIdle()

        assertFalse(viewModel.isDeletingNode.value)
        assertEquals(errorMessage, viewModel.deleteNodeError.value)
        assertFalse(viewModel.deleteNodeSuccess.value)
    }

    @Test
    fun `refreshNodeConnections should refetch node connections`() = runTest {
        setupViewModelWithMocks()
        val mockEdges = listOf(
            SpaceEdge(1, 456, 1, "Edge 1", null)
        )
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(mockEdges))
        initializeViewModel()

        viewModel.refreshNodeConnections()
        advanceUntilIdle()

        verify(mockRepository).getSpaceEdges(spaceId)
    }

    @Test
    fun `filteredConnections should filter by search query`() = runTest {
        setupViewModelWithMocks()
        val mockEdges = listOf(
            SpaceEdge(1, 456, 1, "Test Edge", null),
            SpaceEdge(2, 1, 456, "Other Edge", null)
        )
        whenever(mockRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(mockEdges))
        initializeViewModel()

        viewModel.updateConnectionSearchQuery("Test")
        advanceUntilIdle()

        val filtered = viewModel.filteredConnections.value
        assertTrue(filtered.all { it.label.contains("Test", ignoreCase = true) })
    }

    @Test
    fun `filteredOptions should filter properties by search query`() = runTest {
        setupViewModelWithMocks()
        val property1 = createMockNodeProperty("prop1", "Test Property", "Value 1")
        val property2 = createMockNodeProperty("prop2", "Other Property", "Value 2")
        whenever(mockRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(listOf(property1, property2)))
        whenever(mockRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(listOf(property1, property2)))
        initializeViewModel()

        viewModel.updateSearchQuery("Test")
        advanceUntilIdle()

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
        whenever(mockCountriesRepository.getCountries())
            .thenReturn(Result.success(emptyList()))
    }

    private suspend fun initializeViewModel() {
        viewModel = SpaceNodeDetailsViewModel(mockRepository, mockSpacesRepository, mockCountriesRepository, savedStateHandle)
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

    @Test
    fun `fetchReportReasons should load node report reasons successfully`() = runTest {
        val reportReasons = listOf(
            com.yybb.myapplication.data.network.dto.ReportReasonItem("INAPPROPRIATE", "Inappropriate content"),
            com.yybb.myapplication.data.network.dto.ReportReasonItem("DUPLICATE_NODE", "Duplicate node")
        )

        setupViewModelWithMocks()
        whenever(mockSpacesRepository.getReportReasons("node"))
            .thenReturn(Result.success(reportReasons))
        initializeViewModel()

        viewModel.fetchReportReasons()
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingReportReasons.value)
        assertEquals(reportReasons, viewModel.reportReasons.value)
        assertNull(viewModel.reportError.value)
    }

    @Test
    fun `fetchReportReasons should set error on failure`() = runTest {
        val errorMessage = "Failed to load report reasons"

        setupViewModelWithMocks()
        whenever(mockSpacesRepository.getReportReasons("node"))
            .thenReturn(Result.failure(Exception(errorMessage)))
        initializeViewModel()

        viewModel.fetchReportReasons()
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingReportReasons.value)
        assertTrue(viewModel.reportReasons.value.isEmpty())
        assertEquals(errorMessage, viewModel.reportError.value)
    }

    @Test
    fun `submitReport should submit node report successfully`() = runTest {
        val submitResponse = com.yybb.myapplication.data.network.dto.SubmitReportResponse(
            id = 1,
            contentType = "node",
            contentId = 456,
            reason = "INAPPROPRIATE",
            status = "OPEN",
            space = 0,
            reporter = 1,
            reporterUsername = "testuser",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            entityReportCount = 1,
            entityIsReported = true
        )

        setupViewModelWithMocks()
        whenever(mockSpacesRepository.submitReport("node", 456, "INAPPROPRIATE"))
            .thenReturn(Result.success(submitResponse))
        initializeViewModel()

        viewModel.submitReport("INAPPROPRIATE")
        advanceUntilIdle()

        assertFalse(viewModel.isSubmittingReport.value)
        assertTrue(viewModel.reportSubmitSuccess.value)
        assertNull(viewModel.reportError.value)
    }

    @Test
    fun `resetReportSubmitSuccess should reset success flag`() = runTest {
        val submitResponse = com.yybb.myapplication.data.network.dto.SubmitReportResponse(
            id = 1,
            contentType = "node",
            contentId = 456,
            reason = "INAPPROPRIATE",
            status = "OPEN",
            space = 0,
            reporter = 1,
            reporterUsername = "testuser",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            entityReportCount = 1,
            entityIsReported = true
        )

        setupViewModelWithMocks()
        whenever(mockSpacesRepository.submitReport("node", 456, "INAPPROPRIATE"))
            .thenReturn(Result.success(submitResponse))
        initializeViewModel()

        viewModel.submitReport("INAPPROPRIATE")
        advanceUntilIdle()

        assertTrue(viewModel.reportSubmitSuccess.value)
        viewModel.resetReportSubmitSuccess()
        assertFalse(viewModel.reportSubmitSuccess.value)
    }

    @Test
    fun `loadCountries should load countries successfully`() = runTest {
        setupViewModelWithMocks()
        val mockCountries = listOf(
            CountryPosition("United States", "US", -95.7129, 37.0902),
            CountryPosition("Turkey", "TR", 35.2433, 38.9637)
        )
        whenever(mockCountriesRepository.getCountries())
            .thenReturn(Result.success(mockCountries))
        initializeViewModel()

        viewModel.loadCountries()
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingCountries.value)
        assertEquals(2, viewModel.countries.value.size)
        assertEquals("Turkey", viewModel.countries.value[0].name) // Sorted by name
        assertEquals("United States", viewModel.countries.value[1].name)
    }

    @Test
    fun `loadCountries should handle error on failure`() = runTest {
        setupViewModelWithMocks()
        whenever(mockCountriesRepository.getCountries())
            .thenReturn(Result.failure(Exception("Failed to load countries")))
        initializeViewModel()

        viewModel.loadCountries()
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingCountries.value)
        assertTrue(viewModel.countries.value.isEmpty())
    }

    @Test
    fun `loadCities should load cities successfully`() = runTest {
        setupViewModelWithMocks()
        val mockCities = listOf("Istanbul", "Ankara", "Izmir")
        whenever(mockCountriesRepository.getCities("Turkey"))
            .thenReturn(Result.success(mockCities))
        initializeViewModel()

        viewModel.loadCities("Turkey")
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingCities.value)
        assertEquals(3, viewModel.cities.value.size)
        assertEquals("Ankara", viewModel.cities.value[0]) // Sorted
        assertEquals("Istanbul", viewModel.cities.value[1])
        assertEquals("Izmir", viewModel.cities.value[2])
    }

    @Test
    fun `loadCities should handle error on failure`() = runTest {
        setupViewModelWithMocks()
        whenever(mockCountriesRepository.getCities("Turkey"))
            .thenReturn(Result.failure(Exception("Failed to load cities")))
        initializeViewModel()

        viewModel.loadCities("Turkey")
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingCities.value)
        assertTrue(viewModel.cities.value.isEmpty())
    }

    @Test
    fun `getCoordinatesFromAddress should get coordinates successfully`() = runTest {
        setupViewModelWithMocks()
        val mockCoordinates = NominatimCoordinates(
            displayName = "Istanbul, Turkey",
            latitude = 41.0082,
            longitude = 28.9784
        )
        whenever(mockRepository.getCoordinatesFromAddress("Istanbul, Turkey"))
            .thenReturn(Result.success(mockCoordinates))
        initializeViewModel()

        viewModel.getCoordinatesFromAddress("Istanbul", "Turkey")
        advanceUntilIdle()

        assertFalse(viewModel.isGettingCoordinates.value)
        assertNotNull(viewModel.coordinatesResult.value)
        assertEquals(41.0082, viewModel.coordinatesResult.value!!.latitude)
        assertEquals(28.9784, viewModel.coordinatesResult.value!!.longitude)
    }

    @Test
    fun `getCoordinatesFromAddress should return null when city or country is null`() = runTest {
        setupViewModelWithMocks()
        initializeViewModel()

        viewModel.getCoordinatesFromAddress(null, "Turkey")
        advanceUntilIdle()

        assertNull(viewModel.coordinatesResult.value)
    }

    @Test
    fun `getCoordinatesFromAddress should handle error on failure`() = runTest {
        setupViewModelWithMocks()
        whenever(mockRepository.getCoordinatesFromAddress("Istanbul, Turkey"))
            .thenReturn(Result.failure(Exception("Failed to get coordinates")))
        initializeViewModel()

        viewModel.getCoordinatesFromAddress("Istanbul", "Turkey")
        advanceUntilIdle()

        assertFalse(viewModel.isGettingCoordinates.value)
        assertNull(viewModel.coordinatesResult.value)
        assertNotNull(viewModel.locationUpdateError.value)
    }

    @Test
    fun `updateNodeLocation should update location successfully`() = runTest {
        setupViewModelWithMocks()
        val updateLocationResponse = UpdateNodeLocationResponse(
            "Location updated successfully",
            NodeLocationData(
                country = "Turkey",
                city = "Istanbul",
                district = null,
                street = null,
                latitude = 41.0082,
                longitude = 28.9784,
                locationName = "Taksim Square"
            )
        )
        val snapshotResponse = CreateSnapshotResponse(1, "2024-01-01")
        val mockNodes = listOf(
            com.yybb.myapplication.data.model.SpaceNode(
                456, "Test Node", "Q123", "Turkey", "Istanbul", null, null, "41.0082", "28.9784", "Taksim Square", 0
            )
        )

        whenever(mockRepository.updateNodeLocation(
            eq(spaceId),
            eq(nodeId),
            eq("Turkey"),
            eq("Istanbul"),
            eq("Taksim Square"),
            eq(41.0082),
            eq(28.9784)
        )).thenReturn(Result.success(updateLocationResponse))
        whenever(mockRepository.createSnapshot(spaceId))
            .thenReturn(Result.success(snapshotResponse))
        whenever(mockRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))
        initializeViewModel()

        viewModel.updateNodeLocation(
            country = "Turkey",
            city = "Istanbul",
            locationName = "Taksim Square",
            latitude = 41.0082,
            longitude = 28.9784
        )
        advanceUntilIdle()

        assertFalse(viewModel.isUpdatingLocation.value)
        assertNull(viewModel.locationUpdateError.value)
        assertFalse(viewModel.showEditLocationDialog.value)
        verify(mockRepository).updateNodeLocation(
            eq(spaceId),
            eq(nodeId),
            eq("Turkey"),
            eq("Istanbul"),
            eq("Taksim Square"),
            eq(41.0082),
            eq(28.9784)
        )
    }

    @Test
    fun `updateNodeLocation should handle error on update failure`() = runTest {
        setupViewModelWithMocks()
        val errorMessage = "Failed to update location"
        whenever(mockRepository.updateNodeLocation(
            eq(spaceId),
            eq(nodeId),
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(Result.failure(Exception(errorMessage)))
        initializeViewModel()

        viewModel.updateNodeLocation(
            country = "Turkey",
            city = "Istanbul",
            locationName = "Taksim Square",
            latitude = 41.0082,
            longitude = 28.9784
        )
        advanceUntilIdle()

        assertFalse(viewModel.isUpdatingLocation.value)
        assertEquals(errorMessage, viewModel.locationUpdateError.value)
    }
}