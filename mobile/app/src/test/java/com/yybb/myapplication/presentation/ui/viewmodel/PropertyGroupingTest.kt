package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.repository.CountriesRepository
import com.yybb.myapplication.data.repository.SpaceNodeDetailsRepository
import com.yybb.myapplication.data.repository.SpaceNodesRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PropertyGroupingTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var addNodeViewModel: AddNodeViewModel
    private lateinit var spaceNodeDetailsViewModel: SpaceNodeDetailsViewModel
    private lateinit var mockNodeDetailsRepository: SpaceNodeDetailsRepository
    private lateinit var mockNodesRepository: SpaceNodesRepository
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
        mockNodeDetailsRepository = mock()
        mockNodesRepository = mock()
        mockSpacesRepository = mock()
        mockCountriesRepository = mock()
    }

    // ============ AddNodeViewModel Grouping Tests ============

    @Test
    fun `AddNodeViewModel getGroupedProperties should group properties with same propertyId`() = runTest {
        setupAddNodeViewModel()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val properties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15"), // Same propertyId
            createMockNodeProperty("stmt3", "P2", "Name", "John"),
            createMockNodeProperty("stmt4", "P2", "Name", "Jane") // Same propertyId
        )
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(properties))

        addNodeViewModel.selectEntity(entity)
        advanceUntilIdle()

        val groups = addNodeViewModel.getGroupedProperties()
        assertEquals(2, groups.size)
        assertEquals("P1", groups[0].propertyId)
        assertEquals("Birth date", groups[0].propertyLabel)
        assertEquals(2, groups[0].properties.size)
        assertEquals("P2", groups[1].propertyId)
        assertEquals("Name", groups[1].propertyLabel)
        assertEquals(2, groups[1].properties.size)
    }


    @Test
    fun `AddNodeViewModel getGroupedProperties should set isAllChecked to true when all properties in group are selected`() = runTest {
        setupAddNodeViewModel()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val properties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15")
        )
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(properties))

        addNodeViewModel.selectEntity(entity)
        advanceUntilIdle()

        // Select all properties in the group
        addNodeViewModel.togglePropertySelection("stmt1")
        addNodeViewModel.togglePropertySelection("stmt2")
        advanceUntilIdle()

        val groups = addNodeViewModel.getGroupedProperties()
        assertEquals(1, groups.size)
        assertTrue(groups[0].isAllChecked)
    }

    @Test
    fun `AddNodeViewModel getGroupedProperties should set isAllChecked to false when not all properties in group are selected`() = runTest {
        setupAddNodeViewModel()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val properties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15")
        )
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(properties))

        addNodeViewModel.selectEntity(entity)
        advanceUntilIdle()

        // Select only one property in the group
        addNodeViewModel.togglePropertySelection("stmt1")
        advanceUntilIdle()

        val groups = addNodeViewModel.getGroupedProperties()
        assertEquals(1, groups.size)
        assertFalse(groups[0].isAllChecked)
    }

    @Test
    fun `AddNodeViewModel togglePropertyGroup should select all properties in group when none are selected`() = runTest {
        setupAddNodeViewModel()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val properties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15"),
            createMockNodeProperty("stmt3", "P1", "Birth date", "1990-02-01")
        )
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(properties))

        addNodeViewModel.selectEntity(entity)
        advanceUntilIdle()

        assertTrue(addNodeViewModel.selectedProperties.value.isEmpty())

        addNodeViewModel.togglePropertyGroup("P1")
        advanceUntilIdle()

        val selected = addNodeViewModel.selectedProperties.value
        assertEquals(3, selected.size)
        assertTrue(selected.contains("stmt1"))
        assertTrue(selected.contains("stmt2"))
        assertTrue(selected.contains("stmt3"))
    }

    @Test
    fun `AddNodeViewModel togglePropertyGroup should deselect all properties in group when all are selected`() = runTest {
        setupAddNodeViewModel()
        val entity = WikidataProperty("Q1", "Entity 1", "Description 1", "url1")
        val properties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15")
        )
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties("Q1"))
            .thenReturn(Result.success(properties))

        addNodeViewModel.selectEntity(entity)
        advanceUntilIdle()

        // Select all properties first
        addNodeViewModel.togglePropertySelection("stmt1")
        addNodeViewModel.togglePropertySelection("stmt2")
        advanceUntilIdle()
        assertTrue(addNodeViewModel.selectedProperties.value.size == 2)

        // Toggle group should deselect all
        addNodeViewModel.togglePropertyGroup("P1")
        advanceUntilIdle()

        val selected = addNodeViewModel.selectedProperties.value
        assertTrue(selected.isEmpty())
    }

    // ============ SpaceNodeDetailsViewModel Grouping Tests ============

    @Test
    fun `SpaceNodeDetailsViewModel getGroupedProperties should group properties with same propertyId`() = runTest {
        setupSpaceNodeDetailsViewModel()
        val properties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15"),
            createMockNodeProperty("stmt3", "P2", "Name", "John"),
            createMockNodeProperty("stmt4", "P2", "Name", "Jane")
        )
        whenever(mockNodeDetailsRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(properties))

        spaceNodeDetailsViewModel = SpaceNodeDetailsViewModel(
            mockNodeDetailsRepository,
            mockSpacesRepository,
            mockCountriesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        val groups = spaceNodeDetailsViewModel.getGroupedProperties()
        assertEquals(2, groups.size)
        assertEquals("P1", groups[0].propertyId)
        assertEquals("Birth date", groups[0].propertyLabel)
        assertEquals(2, groups[0].properties.size)
    }

    @Test
    fun `SpaceNodeDetailsViewModel getGroupedProperties should set hasSavedProperties to true when group contains saved properties`() = runTest {
        setupSpaceNodeDetailsViewModel()
        val savedProperties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01")
        )
        val availableProperties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15")
        )

        whenever(mockNodeDetailsRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(savedProperties))
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(availableProperties))

        spaceNodeDetailsViewModel = SpaceNodeDetailsViewModel(
            mockNodeDetailsRepository,
            mockSpacesRepository,
            mockCountriesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        val groups = spaceNodeDetailsViewModel.getGroupedProperties()
        assertEquals(1, groups.size)
        assertTrue(groups[0].hasSavedProperties)
    }

    @Test
    fun `SpaceNodeDetailsViewModel getGroupedProperties should set hasSavedProperties to false when group contains no saved properties`() = runTest {
        setupSpaceNodeDetailsViewModel()
        val savedProperties = listOf(
            createMockNodeProperty("stmt3", "P2", "Name", "John")
        )
        val availableProperties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15")
        )

        whenever(mockNodeDetailsRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(savedProperties))
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(availableProperties))

        spaceNodeDetailsViewModel = SpaceNodeDetailsViewModel(
            mockNodeDetailsRepository,
            mockSpacesRepository,
            mockCountriesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        val groups = spaceNodeDetailsViewModel.getGroupedProperties()
        assertEquals(1, groups.size)
        assertFalse(groups[0].hasSavedProperties)
    }

    @Test
    fun `SpaceNodeDetailsViewModel togglePropertyGroup should select all properties when none are selected`() = runTest {
        setupSpaceNodeDetailsViewModel()
        val properties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15")
        )

        whenever(mockNodeDetailsRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(properties))

        spaceNodeDetailsViewModel = SpaceNodeDetailsViewModel(
            mockNodeDetailsRepository,
            mockSpacesRepository,
            mockCountriesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        // Initially, all should be unchecked
        val initialOptions = spaceNodeDetailsViewModel.propertyOptions.value
        assertTrue(initialOptions.all { !it.isChecked })

        spaceNodeDetailsViewModel.togglePropertyGroup("P1")
        advanceUntilIdle()

        val updatedOptions = spaceNodeDetailsViewModel.propertyOptions.value
        val p1Options = updatedOptions.filter { it.property.propertyId == "P1" }
        assertTrue(p1Options.all { it.isChecked })
    }

    @Test
    fun `SpaceNodeDetailsViewModel togglePropertyGroup should keep saved properties checked when deselecting`() = runTest {
        setupSpaceNodeDetailsViewModel()
        val savedProperties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01")
        )
        val availableProperties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15")
        )

        whenever(mockNodeDetailsRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(savedProperties))
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(availableProperties))

        spaceNodeDetailsViewModel = SpaceNodeDetailsViewModel(
            mockNodeDetailsRepository,
            mockSpacesRepository,
            mockCountriesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        // Select the unsaved property too
        spaceNodeDetailsViewModel.togglePropertySelection("stmt2")
        advanceUntilIdle()

        // Now toggle the group (should deselect, but keep saved property checked)
        spaceNodeDetailsViewModel.togglePropertyGroup("P1")
        advanceUntilIdle()

        val updatedOptions = spaceNodeDetailsViewModel.propertyOptions.value
        val stmt1Option = updatedOptions.find { it.property.statementId == "stmt1" }
        val stmt2Option = updatedOptions.find { it.property.statementId == "stmt2" }

        assertNotNull(stmt1Option)
        assertNotNull(stmt2Option)
        assertTrue(stmt1Option!!.isChecked) // Saved property should remain checked
        assertFalse(stmt2Option!!.isChecked) // Unsaved property should be unchecked
    }

    @Test
    fun `SpaceNodeDetailsViewModel deletePropertyGroup should delete all properties in group successfully`() = runTest {
        setupSpaceNodeDetailsViewModel()
        val savedProperties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15")
        )

        // Setup: first call returns savedProperties (for init), second call returns emptyList (after deletion)
        whenever(mockNodeDetailsRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(savedProperties))
            .thenReturn(Result.success(emptyList()))
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(savedProperties))
        whenever(mockNodeDetailsRepository.deleteNodeProperty(spaceId, nodeId, "stmt1"))
            .thenReturn(Result.success(Unit))
        whenever(mockNodeDetailsRepository.deleteNodeProperty(spaceId, nodeId, "stmt2"))
            .thenReturn(Result.success(Unit))

        spaceNodeDetailsViewModel = SpaceNodeDetailsViewModel(
            mockNodeDetailsRepository,
            mockSpacesRepository,
            mockCountriesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        spaceNodeDetailsViewModel.deletePropertyGroup("P1")
        advanceUntilIdle()

        verify(mockNodeDetailsRepository).deleteNodeProperty(spaceId, nodeId, "stmt1")
        verify(mockNodeDetailsRepository).deleteNodeProperty(spaceId, nodeId, "stmt2")
        assertNotNull(spaceNodeDetailsViewModel.nodePropertyDeletionMessage.value)
    }


    @Test
    fun `SpaceNodeDetailsViewModel getPropertyItems should return groups and single items`() = runTest {
        setupSpaceNodeDetailsViewModel()
        val properties = listOf(
            createMockNodeProperty("stmt1", "P1", "Birth date", "1990-01-01"),
            createMockNodeProperty("stmt2", "P1", "Birth date", "1990-01-15"), // Group
            createMockNodeProperty("stmt3", "P2", "Name", "John") // Single
        )

        whenever(mockNodeDetailsRepository.getNodeProperties(spaceId, nodeId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockNodeDetailsRepository.getWikidataEntityProperties(wikidataId))
            .thenReturn(Result.success(properties))

        spaceNodeDetailsViewModel = SpaceNodeDetailsViewModel(
            mockNodeDetailsRepository,
            mockSpacesRepository,
            mockCountriesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        val items = spaceNodeDetailsViewModel.getPropertyItems()
        assertEquals(2, items.size)
        assertTrue(items[0] is SpaceNodeDetailsViewModel.PropertyItem.Group)
        assertTrue(items[1] is SpaceNodeDetailsViewModel.PropertyItem.Single)

        val group = (items[0] as SpaceNodeDetailsViewModel.PropertyItem.Group).group
        assertEquals("P1", group.propertyId)
        assertEquals(2, group.properties.size)

        val single = (items[1] as SpaceNodeDetailsViewModel.PropertyItem.Single).option
        assertEquals("P2", single.property.propertyId)
    }


    private suspend fun setupAddNodeViewModel() {
        savedStateHandle = SavedStateHandle(mapOf("spaceId" to spaceId))
        whenever(mockNodesRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockCountriesRepository.getCountries())
            .thenReturn(Result.success(emptyList()))
        addNodeViewModel = AddNodeViewModel(
            mockNodeDetailsRepository,
            mockNodesRepository,
            mockCountriesRepository,
            savedStateHandle
        )
    }

    private suspend fun setupSpaceNodeDetailsViewModel() {
        savedStateHandle = SavedStateHandle(
            mapOf(
                "spaceId" to spaceId,
                "nodeId" to nodeId,
                "nodeLabel" to nodeLabel,
                "nodeWikidataId" to wikidataId
            )
        )
        whenever(mockNodeDetailsRepository.getSpaceNodes(spaceId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockNodeDetailsRepository.getSpaceEdges(spaceId))
            .thenReturn(Result.success(emptyList()))
        whenever(mockCountriesRepository.getCountries())
            .thenReturn(Result.success(emptyList()))
    }

    private fun createMockNodeProperty(
        statementId: String,
        propertyId: String,
        propertyLabel: String,
        valueText: String
    ): NodeProperty {
        return NodeProperty(
            statementId = statementId,
            propertyId = propertyId,
            propertyLabel = propertyLabel,
            valueText = valueText,
            isEntity = false,
            entityId = null,
            display = "$propertyLabel: $valueText"
        )
    }
}

