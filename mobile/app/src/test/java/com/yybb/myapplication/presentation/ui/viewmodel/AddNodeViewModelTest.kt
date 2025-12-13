package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.network.dto.AddNodeResponse
import com.yybb.myapplication.data.network.dto.CountryPosition
import com.yybb.myapplication.data.network.dto.CreateSnapshotResponse
import com.yybb.myapplication.data.network.dto.NodeLocationData
import com.yybb.myapplication.data.network.dto.NominatimCoordinates
import com.yybb.myapplication.data.network.dto.UpdateNodeLocationResponse
import com.yybb.myapplication.data.repository.CountriesRepository
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
    private lateinit var mockCountriesRepository: CountriesRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val spaceId = "space123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockNodeDetailsRepository = mock()
        mockNodesRepository = mock()
        mockCountriesRepository = mock()
        savedStateHandle = SavedStateHandle(mapOf("spaceId" to spaceId))
    }

    @Test
    fun `initialization should load space nodes`() = runTest {
        val mockNodes = listOf(
            SpaceNode(1, "Node 1", null, null, null, null, null, null, null, null, 0),
            SpaceNode(2, "Node 2", null, null, null, null, null, null, null, null, 0)
        )
        whenever(mockNodesRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(mockNodes))

        viewModel = AddNodeViewModel(mockNodeDetailsRepository, mockNodesRepository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingNodes.value)
        assertEquals(2, viewModel.availableNodes.value.size)
    }

    @Test
    fun `searchWikidataEntities should search entities successfully`() = runTest {
        setupViewModelWithMocks()
        val query = "test entity"
        val mockResults = listOf(
            WikidataProperty("Q1", "Entity 1", "Description 1", "url1"),
            WikidataProperty("Q2", "Entity 2", "Description 2", "url2")
        )
        whenever(mockNodeDetailsRepository.searchWikidataEntities(query))
            .thenReturn(Result.success(mockResults))

        viewModel.searchWikidataEntities(query)
        advanceUntilIdle()

        assertFalse(viewModel.isSearchingWikidata.value)
        assertEquals(2, viewModel.wikidataSearchResults.value.size)
        assertNull(viewModel.wikidataSearchError.value)
    }

    @Test
    fun `searchWikidataEntities should handle error on search failure`() = runTest {
        setupViewModelWithMocks()
        val query = "test entity"
        val errorMessage = "Search failed"
        whenever(mockNodeDetailsRepository.searchWikidataEntities(query))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.searchWikidataEntities(query)
        advanceUntilIdle()

        assertFalse(viewModel.isSearchingWikidata.value)
        assertTrue(viewModel.wikidataSearchResults.value.isEmpty())
        assertEquals(errorMessage, viewModel.wikidataSearchError.value)
    }

    @Test
    fun `selectEntity should set selected entity and load properties`() = runTest {
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val mockProperties = listOf(
            createMockNodeProperty("prop1", "Property 1", "Value 1")
        )
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(mockProperties))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        assertEquals(entity, viewModel.selectedEntity.value)
        assertTrue(viewModel.wikidataSearchResults.value.isEmpty())
        assertEquals(1, viewModel.entityProperties.value.size)
        assertFalse(viewModel.isLoadingProperties.value)
    }

    @Test
    fun `selectEntity should handle error on properties load failure`() = runTest {
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val errorMessage = "Properties load failed"
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        assertEquals(entity, viewModel.selectedEntity.value)
        assertTrue(viewModel.entityProperties.value.isEmpty())
        assertEquals(errorMessage, viewModel.propertiesError.value)
    }

    @Test
    fun `updatePropertySearchQuery should update search query`() = runTest {
        setupViewModelWithMocks()
        val query = "test query"

        viewModel.updatePropertySearchQuery(query)
        advanceUntilIdle()

        assertEquals(query, viewModel.propertySearchQuery.value)
    }

    @Test
    fun `togglePropertySelection should add property to selected`() = runTest {
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        assertTrue(viewModel.selectedProperties.value.contains("prop1"))
    }

    @Test
    fun `togglePropertySelection should remove property from selected when already selected`() = runTest {
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()
        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        viewModel.togglePropertySelection("prop1")
        advanceUntilIdle()

        assertFalse(viewModel.selectedProperties.value.contains("prop1"))
    }

    @Test
    fun `searchEdgeLabelOptions should search with query length bigger than 2`() = runTest {
        setupViewModelWithMocks()
        val query = "test"
        val mockResults = listOf(
            WikidataProperty("P1", "Property 1", "Description 1", "url1")
        )
        whenever(mockNodeDetailsRepository.searchWikidataProperties(query))
            .thenReturn(Result.success(mockResults))

        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertEquals(1, viewModel.edgeLabelSearchResults.value.size)
    }

    @Test
    fun `searchEdgeLabelOptions should not search with query length less than or equal 2`() = runTest {
        setupViewModelWithMocks()
        val query = "te"

        viewModel.searchEdgeLabelOptions(query)
        advanceUntilIdle()

        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
    }

    @Test
    fun `resetEdgeLabelSearch should clear search results`() = runTest {
        setupViewModelWithMocks()

        viewModel.resetEdgeLabelSearch()
        advanceUntilIdle()

        assertFalse(viewModel.isEdgeLabelSearching.value)
        assertTrue(viewModel.edgeLabelSearchResults.value.isEmpty())
        assertNull(viewModel.edgeLabelSearchError.value)
    }

    @Test
    fun `createNode should fail when no entity is selected`() = runTest {
        setupViewModelWithMocks()

        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        assertNotNull(viewModel.createNodeError.value)
        assertTrue(viewModel.createNodeError.value!!.contains("select an entity"))
    }

    @Test
    fun `createNode should fail when relatedNodeId is provided but edgeLabel is blank`() = runTest {
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        viewModel.createNode("node123", "", true, null)
        advanceUntilIdle()

        assertNotNull(viewModel.createNodeError.value)
        assertTrue(viewModel.createNodeError.value!!.contains("edge label"))
    }

    @Test
    fun `createNode should create node successfully without connection`() = runTest {
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

        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        assertFalse(viewModel.isCreatingNode.value)
        assertNotNull(viewModel.createNodeSuccess.value)
        assertEquals("Node created successfully", viewModel.createNodeSuccess.value)
    }

    @Test
    fun `createNode should create node with connection successfully`() = runTest {
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

        viewModel.createNode("node123", "connected to", true, edgeProperty)
        advanceUntilIdle()

        assertFalse(viewModel.isCreatingNode.value)
        assertNotNull(viewModel.createNodeSuccess.value)
        verify(mockNodeDetailsRepository).addNode(eq(spaceId), any())
    }

    @Test
    fun `createNode should handle error on node creation failure`() = runTest {
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

        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        assertFalse(viewModel.isCreatingNode.value)
        assertEquals(errorMessage, viewModel.createNodeError.value)
    }

    @Test
    fun `clearWikidataSearchError should clear error`() = runTest {
        setupViewModelWithMocks()
        viewModel.searchWikidataEntities("test")
        advanceUntilIdle()

        viewModel.clearWikidataSearchError()
        advanceUntilIdle()

        assertNull(viewModel.wikidataSearchError.value)
    }

    @Test
    fun `clearCreateNodeError should clear error`() = runTest {
        setupViewModelWithMocks()
        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        viewModel.clearCreateNodeError()
        advanceUntilIdle()

        assertNull(viewModel.createNodeError.value)
    }

    @Test
    fun `clearCreateNodeSuccess should clear success message`() = runTest {
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

        viewModel.clearCreateNodeSuccess()
        advanceUntilIdle()

        assertNull(viewModel.createNodeSuccess.value)
    }

    @Test
    fun `filteredProperties should filter by search query`() = runTest {
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property1 = createMockNodeProperty("prop1", "Test Property", "Value 1")
        val property2 = createMockNodeProperty("prop2", "Other Property", "Value 2")
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property1, property2)))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        viewModel.updatePropertySearchQuery("Test")
        advanceUntilIdle()

        val filtered = viewModel.filteredProperties.value
        assertTrue(filtered.all { 
            it.propertyLabel.contains("Test", ignoreCase = true) || 
            it.valueText.contains("Test", ignoreCase = true) 
        })
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
        whenever(mockNodeDetailsRepository.getCoordinatesFromAddress("Istanbul, Turkey"))
            .thenReturn(Result.success(mockCoordinates))

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

        viewModel.getCoordinatesFromAddress(null, "Turkey")
        advanceUntilIdle()

        assertNull(viewModel.coordinatesResult.value)
    }

    @Test
    fun `getCoordinatesFromAddress should handle error on failure`() = runTest {
        setupViewModelWithMocks()
        whenever(mockNodeDetailsRepository.getCoordinatesFromAddress("Istanbul, Turkey"))
            .thenReturn(Result.failure(Exception("Failed to get coordinates")))

        viewModel.getCoordinatesFromAddress("Istanbul", "Turkey")
        advanceUntilIdle()

        assertFalse(viewModel.isGettingCoordinates.value)
        assertNull(viewModel.coordinatesResult.value)
    }

    @Test
    fun `saveLocationData should save location data successfully`() = runTest {
        setupViewModelWithMocks()

        viewModel.saveLocationData(
            country = "Turkey",
            city = "Istanbul",
            locationName = "Taksim Square",
            latitude = 41.0370,
            longitude = 28.9850
        )
        advanceUntilIdle()

        assertNotNull(viewModel.locationData.value)
        assertEquals("Turkey", viewModel.locationData.value!!.country)
        assertEquals("Istanbul", viewModel.locationData.value!!.city)
        assertEquals("Taksim Square", viewModel.locationData.value!!.locationName)
        assertEquals(41.0370, viewModel.locationData.value!!.latitude)
        assertEquals(28.9850, viewModel.locationData.value!!.longitude)
        assertFalse(viewModel.showLocationDialog.value)
    }

    @Test
    fun `saveLocationData should filter blank location name`() = runTest {
        setupViewModelWithMocks()

        viewModel.saveLocationData(
            country = "Turkey",
            city = "Istanbul",
            locationName = "   ",
            latitude = null,
            longitude = null
        )
        advanceUntilIdle()

        assertNotNull(viewModel.locationData.value)
        assertNull(viewModel.locationData.value!!.locationName)
    }

    @Test
    fun `createNode should include location data in request`() = runTest {
        setupViewModelWithMocks()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val property = createMockNodeProperty("prop1", "Property 1", "Value 1")
        val addNodeResponse = AddNodeResponse("Node created successfully")
        val snapshotResponse = CreateSnapshotResponse(1, "2024-01-01")
        val updateLocationResponse = UpdateNodeLocationResponse(
            "Location updated",
            NodeLocationData(
                country = "Turkey",
                city = "Istanbul",
                district = null,
                street = null,
                latitude = 41.0370,
                longitude = 28.9850,
                locationName = "Taksim Square"
            )
        )

        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(listOf(property)))
        whenever(mockNodeDetailsRepository.addNode(eq(spaceId), any()))
            .thenReturn(Result.success(addNodeResponse))
        whenever(mockNodeDetailsRepository.createSnapshot(spaceId))
            .thenReturn(Result.success(snapshotResponse))
        whenever(mockNodeDetailsRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(listOf(
                SpaceNode(1, "Entity 1", "Q1", null, null, null, null, null, null, null, 0)
            )))
        whenever(mockNodeDetailsRepository.updateNodeLocation(
            eq(spaceId),
            eq("1"),
            eq("Turkey"),
            eq("Istanbul"),
            eq("Taksim Square"),
            eq(41.0370),
            eq(28.9850)
        )).thenReturn(Result.success(updateLocationResponse))

        viewModel.selectEntity(entity)
        advanceUntilIdle()

        viewModel.saveLocationData(
            country = "Turkey",
            city = "Istanbul",
            locationName = "Taksim Square",
            latitude = 41.0370,
            longitude = 28.9850
        )
        advanceUntilIdle()

        viewModel.createNode(null, "", true, null)
        advanceUntilIdle()

        assertFalse(viewModel.isCreatingNode.value)
        assertNotNull(viewModel.createNodeSuccess.value)
        verify(mockNodeDetailsRepository).addNode(eq(spaceId), any())
        verify(mockNodeDetailsRepository).updateNodeLocation(
            eq(spaceId),
            eq("1"),
            eq("Turkey"),
            eq("Istanbul"),
            eq("Taksim Square"),
            eq(41.0370),
            eq(28.9850)
        )
    }

    @Test
    fun `showLocationDialog should set showLocationDialog to true`() = runTest {
        setupViewModelWithMocks()

        viewModel.showLocationDialog()
        advanceUntilIdle()

        assertTrue(viewModel.showLocationDialog.value)
    }

    @Test
    fun `hideLocationDialog should set showLocationDialog to false`() = runTest {
        setupViewModelWithMocks()
        viewModel.showLocationDialog()
        advanceUntilIdle()

        viewModel.hideLocationDialog()
        advanceUntilIdle()

        assertFalse(viewModel.showLocationDialog.value)
    }

    // Helper functions
    private suspend fun setupViewModelWithMocks() {
        whenever(mockNodesRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockCountriesRepository.getCountries())
            .thenReturn(Result.success(emptyList()))
        viewModel = AddNodeViewModel(mockNodeDetailsRepository, mockNodesRepository, mockCountriesRepository, savedStateHandle)
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

