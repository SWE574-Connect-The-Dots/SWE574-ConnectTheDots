package com.yybb.myapplication.presentation.ui.viewmodel

import app.cash.turbine.test
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.network.dto.SpaceDetailsResponse
import com.yybb.myapplication.data.network.dto.SpaceMembershipResponse
import com.yybb.myapplication.data.network.dto.SpaceTagDto
import com.yybb.myapplication.data.repository.SpacesRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SpacesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SpacesViewModel
    private lateinit var mockRepository: SpacesRepository
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository

    private val currentUsername = "dogaunal"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        mockUserPreferencesRepository = mock()
        whenever(mockUserPreferencesRepository.username).thenReturn(flowOf(currentUsername))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestSpaceDetailsResponse(
        id: Int = 1,
        title: String = "Test Space",
        description: String = "Test Description",
        creatorUsername: String = "creator",
        collaborators: List<String> = emptyList(),
        tags: List<SpaceTagDto> = listOf(SpaceTagDto(1, "tag1", "Q1", "Tag 1")),
        isArchived: Boolean = false
    ): SpaceDetailsResponse {
        return SpaceDetailsResponse(
            id = id,
            title = title,
            description = description,
            createdAt = "2023-01-01T00:00:00.000000Z",
            creatorUsername = creatorUsername,
            tags = tags,
            collaborators = collaborators,
            isArchived = isArchived
        )
    }

    @Test
    fun `init should load trending spaces`() = runTest {
        val spaces = listOf(createTestSpaceDetailsResponse())
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))
        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()
        verify(mockRepository).getTrendingSpaces()
        assertEquals(spaces.size, viewModel.allSpaces.value.size)
        assertEquals(SpacesSection.TRENDING, viewModel.selectedSection.value)
    }

    @Test
    fun `init should set loading state during fetch`() = runTest {
        val spaces = listOf(createTestSpaceDetailsResponse())
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))
        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.allSpaces.value.isNotEmpty())
    }

    @Test
    fun `init should handle error when loading trending spaces fails`() = runTest {
        val errorMessage = "Network error"
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.failure(Exception(errorMessage)))
        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()
        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `selectSection should load trending spaces when TRENDING selected`() = runTest {
        val trendingSpaces = listOf(createTestSpaceDetailsResponse(id = 1))
        val newSpaces = listOf(createTestSpaceDetailsResponse(id = 2))
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(trendingSpaces))
        whenever(mockRepository.getNewSpaces()).thenReturn(Result.success(newSpaces))
        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()
        verify(mockRepository).getTrendingSpaces()
        viewModel.selectSection(SpacesSection.TRENDING)
        advanceUntilIdle()

        assertEquals(SpacesSection.TRENDING, viewModel.selectedSection.value)
        verify(mockRepository, org.mockito.kotlin.times(2)).getTrendingSpaces()
    }

    @Test
    fun `selectSection should load new spaces when NEW selected`() = runTest {
        val trendingSpaces = listOf(createTestSpaceDetailsResponse(id = 1))
        val newSpaces = listOf(createTestSpaceDetailsResponse(id = 2))
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(trendingSpaces))
        whenever(mockRepository.getNewSpaces()).thenReturn(Result.success(newSpaces))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()
        viewModel.selectSection(SpacesSection.NEW)
        advanceUntilIdle()
        assertEquals(SpacesSection.NEW, viewModel.selectedSection.value)
        verify(mockRepository).getNewSpaces()
    }

    @Test
    fun `loadTrendingSpaces should update allSpaces with correct data`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(
                id = 1,
                title = "Space 1",
                collaborators = listOf(currentUsername)
            ),
            createTestSpaceDetailsResponse(
                id = 2,
                title = "Space 2",
                collaborators = listOf("otheruser")
            )
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val allSpaces = viewModel.allSpaces.value
        assertEquals(2, allSpaces.size)
        assertEquals("Space 1", allSpaces[0].title)
        assertTrue(allSpaces[0].isJoined)
        assertEquals("Space 2", allSpaces[1].title)
        assertFalse(allSpaces[1].isJoined)
    }

    @Test
    fun `loadNewSpaces should update allSpaces with correct data`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(
                id = 1,
                title = "New Space 1",
                collaborators = listOf(currentUsername)
            )
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(emptyList()))
        whenever(mockRepository.getNewSpaces()).thenReturn(Result.success(spaces))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.selectSection(SpacesSection.NEW)
        advanceUntilIdle()

        val allSpaces = viewModel.allSpaces.value
        assertEquals(1, allSpaces.size)
        assertEquals("New Space 1", allSpaces[0].title)
        assertTrue(allSpaces[0].isJoined)
    }

    @Test
    fun `updateSearchQuery should filter spaces by title`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(id = 1, title = "AI Research"),
            createTestSpaceDetailsResponse(id = 2, title = "Machine Learning")
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        
        viewModel.filteredSpaces.test {
            val initial = awaitItem()
            assertEquals(0, initial.size)

            advanceUntilIdle()
            val allSpaces = awaitItem()
            assertEquals(2, allSpaces.size)

            viewModel.updateSearchQuery("AI")
            advanceUntilIdle()

            val filtered = awaitItem()
            assertEquals(1, filtered.size)
            assertEquals("AI Research", filtered[0].title)
        }
    }

    @Test
    fun `updateSearchQuery should filter spaces by description`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(id = 1, description = "Discussion about artificial intelligence"),
            createTestSpaceDetailsResponse(id = 2, description = "Discussion about cooking")
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        
        viewModel.filteredSpaces.test {
            val initial = awaitItem()
            assertEquals(0, initial.size)

            advanceUntilIdle()
            val allSpaces = awaitItem()
            assertEquals(2, allSpaces.size)

            viewModel.updateSearchQuery("artificial")
            advanceUntilIdle()

            val filtered = awaitItem()
            assertEquals(1, filtered.size)
            assertTrue(filtered[0].description.contains("artificial intelligence"))
        }
    }

    @Test
    fun `updateSearchQuery should filter spaces by creator username`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(id = 1, creatorUsername = "alice"),
            createTestSpaceDetailsResponse(id = 2, creatorUsername = "bob")
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        
        viewModel.filteredSpaces.test {
            val initial = awaitItem()
            assertEquals(0, initial.size)

            advanceUntilIdle()
            val allSpaces = awaitItem()
            assertEquals(2, allSpaces.size)

            viewModel.updateSearchQuery("alice")
            advanceUntilIdle()

            val filtered = awaitItem()
            assertEquals(1, filtered.size)
            assertEquals("alice", filtered[0].creatorUsername)
        }
    }

    @Test
    fun `updateSearchQuery should filter spaces by tag name`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(
                id = 1,
                tags = listOf(SpaceTagDto(1, "AI", "Q1", "Artificial Intelligence"))
            ),
            createTestSpaceDetailsResponse(
                id = 2,
                tags = listOf(SpaceTagDto(2, "Cooking", "Q2", "Cooking"))
            )
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        
        viewModel.filteredSpaces.test {
            val initial = awaitItem()
            assertEquals(0, initial.size)

            advanceUntilIdle()
            val allSpaces = awaitItem()
            assertEquals(2, allSpaces.size)

            viewModel.updateSearchQuery("AI")
            advanceUntilIdle()

            val filtered = awaitItem()
            assertEquals(1, filtered.size)
            assertTrue(filtered[0].tags.any { it.name == "AI" })
        }
    }

    @Test
    fun `onJoinSpaceClick should join space and refresh spaces then navigate`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(
                id = 1,
                collaborators = emptyList()
            )
        )
        val updatedSpaces = listOf(
            createTestSpaceDetailsResponse(
                id = 1,
                collaborators = listOf(currentUsername)
            )
        )
        val membershipResponse = SpaceMembershipResponse("Success", true)
        
        whenever(mockRepository.getTrendingSpaces())
            .thenReturn(Result.success(spaces))
            .thenReturn(Result.success(updatedSpaces))
        whenever(mockRepository.joinSpace("1")).thenReturn(Result.success(membershipResponse))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.onJoinSpaceClick(1)
        advanceUntilIdle()

        verify(mockRepository).joinSpace("1")
        verify(mockRepository, org.mockito.kotlin.times(2)).getTrendingSpaces()
        
        viewModel.eventFlow.test {
            val event = awaitItem()
            assertTrue(event is SpacesEvent.NavigateToSpaceDetails)
            assertEquals(1, (event as SpacesEvent.NavigateToSpaceDetails).spaceId)
        }
    }

    @Test
    fun `onJoinSpaceClick should refresh new spaces when NEW section is selected`() = runTest {
        val spaces = listOf(createTestSpaceDetailsResponse(id = 1))
        val membershipResponse = SpaceMembershipResponse("Success", true)
        
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(emptyList()))
        whenever(mockRepository.getNewSpaces())
            .thenReturn(Result.success(spaces))
            .thenReturn(Result.success(spaces))
        whenever(mockRepository.joinSpace("1")).thenReturn(Result.success(membershipResponse))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.selectSection(SpacesSection.NEW)
        advanceUntilIdle()

        viewModel.onJoinSpaceClick(1)
        advanceUntilIdle()

        verify(mockRepository).joinSpace("1")
        verify(mockRepository, org.mockito.kotlin.times(2)).getNewSpaces()
    }

    @Test
    fun `onJoinSpaceClick should set error on join failure`() = runTest {
        val spaces = listOf(createTestSpaceDetailsResponse(id = 1))
        val errorMessage = "Join failed"
        
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))
        whenever(mockRepository.joinSpace("1")).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.onJoinSpaceClick(1)
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onJoinSpaceClick should set error on refresh failure after successful join`() = runTest {
        val spaces = listOf(createTestSpaceDetailsResponse(id = 1))
        val membershipResponse = SpaceMembershipResponse("Success", true)
        val errorMessage = "Refresh failed"
        
        whenever(mockRepository.getTrendingSpaces())
            .thenReturn(Result.success(spaces))
            .thenReturn(Result.failure(Exception(errorMessage)))
        whenever(mockRepository.joinSpace("1")).thenReturn(Result.success(membershipResponse))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.onJoinSpaceClick(1)
        advanceUntilIdle()

        verify(mockRepository).joinSpace("1")
        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onLeaveSpaceClick should leave space and refresh spaces`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(
                id = 1,
                collaborators = listOf(currentUsername)
            )
        )
        val membershipResponse = SpaceMembershipResponse("Success", true)
        
        whenever(mockRepository.getTrendingSpaces())
            .thenReturn(Result.success(spaces))
            .thenReturn(Result.success(spaces))
        whenever(mockRepository.leaveSpace("1")).thenReturn(Result.success(membershipResponse))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.onLeaveSpaceClick(1)
        advanceUntilIdle()

        verify(mockRepository).leaveSpace("1")
        verify(mockRepository, org.mockito.kotlin.times(2)).getTrendingSpaces()
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onLeaveSpaceClick should refresh new spaces when NEW section is selected`() = runTest {
        val spaces = listOf(createTestSpaceDetailsResponse(id = 1))
        val membershipResponse = SpaceMembershipResponse("Success", true)
        
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(emptyList()))
        whenever(mockRepository.getNewSpaces())
            .thenReturn(Result.success(spaces))
            .thenReturn(Result.success(spaces))
        whenever(mockRepository.leaveSpace("1")).thenReturn(Result.success(membershipResponse))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.selectSection(SpacesSection.NEW)
        advanceUntilIdle()

        viewModel.onLeaveSpaceClick(1)
        advanceUntilIdle()

        verify(mockRepository).leaveSpace("1")
        verify(mockRepository, org.mockito.kotlin.times(2)).getNewSpaces()
    }

    @Test
    fun `onLeaveSpaceClick should set error on leave failure`() = runTest {
        val spaces = listOf(createTestSpaceDetailsResponse(id = 1))
        val errorMessage = "Leave failed"
        
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))
        whenever(mockRepository.leaveSpace("1")).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.onLeaveSpaceClick(1)
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onGoToDetailsClicked should emit NavigateToSpaceDetails event`() = runTest {
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(emptyList()))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.onGoToDetailsClicked(123)

        viewModel.eventFlow.test {
            val event = awaitItem()
            assertTrue(event is SpacesEvent.NavigateToSpaceDetails)
            assertEquals(123, (event as SpacesEvent.NavigateToSpaceDetails).spaceId)
        }
    }

    @Test
    fun `clearError should reset error to null`() = runTest {
        val spaces = listOf(createTestSpaceDetailsResponse(id = 1))
        val errorMessage = "Test error"
        
        whenever(mockRepository.getTrendingSpaces())
            .thenReturn(Result.success(spaces))
            .thenReturn(Result.failure(Exception(errorMessage)))
        whenever(mockRepository.joinSpace("1")).thenReturn(Result.success(SpaceMembershipResponse("Success", true)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.onJoinSpaceClick(1)
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)

        viewModel.clearError()

        assertNull(viewModel.error.value)
    }

    @Test
    fun `toSpaceListItem should set isJoined to true when user is in collaborators`() = runTest {
        val spaceResponse = createTestSpaceDetailsResponse(
            id = 1,
            collaborators = listOf(currentUsername, "otheruser")
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(listOf(spaceResponse)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val spaceItem = viewModel.allSpaces.value.first()
        assertTrue(spaceItem.isJoined)
        assertEquals(2, spaceItem.collaboratorsCount)
    }

    @Test
    fun `toSpaceListItem should set isJoined to false when user is not in collaborators`() = runTest {
        val spaceResponse = createTestSpaceDetailsResponse(
            id = 1,
            collaborators = listOf("otheruser1", "otheruser2")
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(listOf(spaceResponse)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val spaceItem = viewModel.allSpaces.value.first()
        assertFalse(spaceItem.isJoined)
        assertEquals(2, spaceItem.collaboratorsCount)
    }

    @Test
    fun `toSpaceListItem should map all fields correctly`() = runTest {
        val spaceResponse = createTestSpaceDetailsResponse(
            id = 42,
            title = "Test Title",
            description = "Test Description",
            creatorUsername = "creator",
            collaborators = listOf("user1", "user2", "user3"),
            tags = listOf(
                SpaceTagDto(1, "Tag1", "Q1", "Tag 1"),
                SpaceTagDto(2, "Tag2", "Q2", "Tag 2")
            ),
            isArchived = false
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(listOf(spaceResponse)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val spaceItem = viewModel.allSpaces.value.first()
        assertEquals(42, spaceItem.id)
        assertEquals("Test Title", spaceItem.title)
        assertEquals("Test Description", spaceItem.description)
        assertEquals("creator", spaceItem.creatorUsername)
        assertEquals(3, spaceItem.collaboratorsCount)
        assertEquals(2, spaceItem.tags.size)
        assertEquals("Tag1", spaceItem.tags[0].name)
        assertEquals("Tag2", spaceItem.tags[1].name)
        assertFalse(spaceItem.isArchived)
    }

    @Test
    fun `toSpaceListItem should map isArchived field correctly`() = runTest {
        val archivedSpaceResponse = createTestSpaceDetailsResponse(
            id = 1,
            isArchived = true
        )
        val nonArchivedSpaceResponse = createTestSpaceDetailsResponse(
            id = 2,
            isArchived = false
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(listOf(archivedSpaceResponse, nonArchivedSpaceResponse)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val spaceItems = viewModel.allSpaces.value
        assertEquals(2, spaceItems.size)
        assertTrue(spaceItems.find { it.id == 1 }?.isArchived == true)
        assertFalse(spaceItems.find { it.id == 2 }?.isArchived == true)
    }

    @Test
    fun `SpaceListItem getTruncatedDescription should truncate long descriptions`() = runTest {
        val longDescription = "a".repeat(250)
        val spaceResponse = createTestSpaceDetailsResponse(
            id = 1,
            description = longDescription
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(listOf(spaceResponse)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val spaceItem = viewModel.allSpaces.value.first()
        val truncated = spaceItem.getTruncatedDescription()
        
        assertTrue(truncated.length <= 203)
        assertTrue(truncated.endsWith("..."))
    }

    @Test
    fun `SpaceListItem getTruncatedDescription should not truncate short descriptions`() = runTest {
        val shortDescription = "Short description"
        val spaceResponse = createTestSpaceDetailsResponse(
            id = 1,
            description = shortDescription
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(listOf(spaceResponse)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val spaceItem = viewModel.allSpaces.value.first()
        val result = spaceItem.getTruncatedDescription()
        
        assertEquals(shortDescription, result)
        assertFalse(result.endsWith("..."))
    }

    @Test
    fun `SpaceListItem getFormattedDate should format date correctly`() = runTest {
        val spaceResponse = createTestSpaceDetailsResponse(
            id = 1
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(listOf(spaceResponse)))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val spaceItem = viewModel.allSpaces.value.first()
        val formattedDate = spaceItem.getFormattedDate()
        
        assertTrue(formattedDate.isNotBlank())
        assertTrue(formattedDate.contains("2023"))
    }

    @Test
    fun `selectedSection should default to TRENDING`() = runTest {
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(emptyList()))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)

        assertEquals(SpacesSection.TRENDING, viewModel.selectedSection.value)
    }

    @Test
    fun `searchQuery should default to empty string`() = runTest {
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(emptyList()))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `initial allSpaces should be empty`() = runTest {
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(emptyList()))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)

        assertTrue(viewModel.allSpaces.value.isEmpty())
    }

    @Test
    fun `initial filteredSpaces should be empty`() = runTest {
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(emptyList()))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.filteredSpaces.test {
            val filtered = awaitItem()
            assertTrue(filtered.isEmpty())
        }
    }

    @Test
    fun `filteredSpaces should update when search query changes`() = runTest {
        val spaces = listOf(
            createTestSpaceDetailsResponse(id = 1, title = "AI"),
            createTestSpaceDetailsResponse(id = 2, title = "Cooking")
        )
        whenever(mockRepository.getTrendingSpaces()).thenReturn(Result.success(spaces))

        viewModel = SpacesViewModel(mockRepository, mockUserPreferencesRepository)
        
        viewModel.filteredSpaces.test {
            awaitItem()

            advanceUntilIdle()

            val allSpaces = awaitItem()
            assertEquals(2, allSpaces.size)

            viewModel.updateSearchQuery("AI")
            advanceUntilIdle()

            val filtered = awaitItem()
            assertEquals(1, filtered.size)
            assertEquals("AI", filtered[0].title)
        }
    }
}

