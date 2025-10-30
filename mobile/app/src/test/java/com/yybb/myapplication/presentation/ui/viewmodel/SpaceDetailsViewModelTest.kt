package com.yybb.myapplication.presentation.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.yybb.myapplication.R
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.Discussion
import com.yybb.myapplication.data.model.SpaceDetails
import com.yybb.myapplication.data.model.SpaceTag
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.network.dto.DiscussionDto
import com.yybb.myapplication.data.network.dto.SpaceMembershipResponse
import com.yybb.myapplication.data.repository.ProfileRepository
import com.yybb.myapplication.data.repository.SpacesRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SpaceDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SpaceDetailsViewModel
    private lateinit var mockContext: Context
    private lateinit var mockSpacesRepository: SpacesRepository
    private lateinit var mockProfileRepository: ProfileRepository
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val testSpaceId = "123"
    private val currentUsername = "dogaunal"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockContext = mock()
        mockSpacesRepository = mock()
        mockProfileRepository = mock()
        mockUserPreferencesRepository = mock()
        savedStateHandle = mock()

        // Setup common mocks
        whenever(savedStateHandle.get<String>("spaceId")).thenReturn(testSpaceId)
        whenever(mockSpacesRepository.isColorBlindTheme).thenReturn(flowOf(false))
        whenever(mockUserPreferencesRepository.getCurrentUsernameSync()).thenReturn(currentUsername)

        // Setup context string resources
        whenever(mockContext.getString(R.string.failed_add_disc_message)).thenReturn("Failed to add discussion")
        whenever(mockContext.getString(R.string.failed_join_space_message)).thenReturn("Failed to join space")
        whenever(mockContext.getString(R.string.failed_leave_space_message)).thenReturn("Failed to leave space")
        whenever(mockContext.getString(R.string.failed_delete_space_message)).thenReturn("Failed to delete space")
        whenever(mockContext.getString(R.string.failed_vote_discussion_message)).thenReturn("Failed to vote on discussion")
        whenever(mockContext.getString(R.string.failed_get_space_det_message)).thenReturn("Failed to get space details")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestSpaceDetails(
        id: Int = 1,
        creatorUsername: String = currentUsername,
        collaborators: List<String> = listOf(currentUsername)
    ): SpaceDetails {
        return SpaceDetails(
            id = id,
            title = "Test Space",
            description = "Test Description",
            createdAt = "2023-01-01T00:00:00Z",
            creatorUsername = creatorUsername,
            tags = listOf(
                SpaceTag(1, "tag1", "Q1", "Tag 1")
            ),
            collaborators = collaborators
        )
    }

    private fun createTestDiscussion(
        id: Int = 1,
        userReaction: String? = null,
        upvotes: Int = 0,
        downvotes: Int = 0
    ): Discussion {
        return Discussion(
            id = id,
            text = "Test discussion",
            createdAt = "2023-01-01T00:00:00.000000Z",
            username = "user1",
            upvotes = upvotes,
            downvotes = downvotes,
            userReaction = userReaction
        )
    }

    private fun createTestDiscussionDto(
        id: Int = 1,
        userReaction: String? = null,
        upvotes: Int = 0,
        downvotes: Int = 0
    ): DiscussionDto {
        return DiscussionDto(
            id = id,
            text = "Test discussion",
            createdAt = "2023-01-01T00:00:00.000000Z",
            username = "user1",
            upvotes = upvotes,
            downvotes = downvotes,
            userReaction = userReaction
        )
    }

    @Test
    fun `init should fetch space details and discussions`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        val discussions = listOf(createTestDiscussion())

        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(discussions))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        assertEquals(spaceDetails, viewModel.spaceDetails.value)
        assertEquals(discussions, viewModel.discussions.value)
    }

    @Test
    fun `init should set loading state during fetch`() = runTest {
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(createTestSpaceDetails()))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )

        assertTrue(viewModel.isLoading.value)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `init should handle error when fetching space details fails`() = runTest {
        val errorMessage = "Network error"
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(
            flow { throw Exception(errorMessage) }
        )
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `addDiscussion should successfully add discussion and refresh list`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        val initialDiscussions = listOf(createTestDiscussion())
        val newDiscussionText = "New discussion"

        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId))
            .thenReturn(flowOf(initialDiscussions))
            .thenReturn(flowOf(initialDiscussions + createTestDiscussion(id = 2)))
        whenever(mockSpacesRepository.addDiscussion(testSpaceId, newDiscussionText))
            .thenReturn(Result.success(Unit))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.addDiscussion(newDiscussionText)
        advanceUntilIdle()

        // After completion, isAddingDiscussion should be false
        assertFalse(viewModel.isAddingDiscussion.value)
        assertNull(viewModel.error.value)
        verify(mockSpacesRepository).addDiscussion(testSpaceId, newDiscussionText)
        // getSpaceDiscussions is called once in init and once after addDiscussion success
        verify(mockSpacesRepository, times(2)).getSpaceDiscussions(testSpaceId)
    }

    @Test
    fun `addDiscussion should set error on failure`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        
        val errorMessage = "Failed to add"
        whenever(mockSpacesRepository.addDiscussion(testSpaceId, "test"))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.addDiscussion("test")
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isAddingDiscussion.value)
    }

    @Test
    fun `isUserCollaborator should return true when user is in collaborators list`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = listOf(currentUsername, "user2"))
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        assertTrue(viewModel.isUserCollaborator())
    }

    @Test
    fun `isUserCollaborator should return false when user is not in collaborators list`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = listOf("user2", "user3"))
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        assertFalse(viewModel.isUserCollaborator())
    }

    @Test
    fun `isUserCreator should return true when user is the creator`() = runTest {
        val spaceDetails = createTestSpaceDetails(creatorUsername = currentUsername)
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        assertTrue(viewModel.isUserCreator())
    }

    @Test
    fun `isUserCreator should return false when user is not the creator`() = runTest {
        val spaceDetails = createTestSpaceDetails(creatorUsername = "otheruser")
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        assertFalse(viewModel.isUserCreator())
    }

    @Test
    fun `joinSpace should successfully join and refresh space details`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = emptyList())
        val membershipResponse = SpaceMembershipResponse("Success", true)
        
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId))
            .thenReturn(flowOf(spaceDetails))
            .thenReturn(flowOf(spaceDetails.copy(collaborators = listOf(currentUsername))))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        whenever(mockSpacesRepository.joinSpace(testSpaceId))
            .thenReturn(Result.success(membershipResponse))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.joinSpace()
        advanceUntilIdle()

        // After completion, isJoiningLeavingSpace should be false
        assertFalse(viewModel.isJoiningLeavingSpace.value)
        assertNull(viewModel.error.value)
        verify(mockSpacesRepository).joinSpace(testSpaceId)
        // getSpaceDetails is called once in init and once after joinSpace success
        verify(mockSpacesRepository, times(2)).getSpaceDetails(testSpaceId)
    }

    @Test
    fun `joinSpace should set error on failure`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        
        val errorMessage = "Join failed"
        whenever(mockSpacesRepository.joinSpace(testSpaceId))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.joinSpace()
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isJoiningLeavingSpace.value)
    }

    @Test
    fun `leaveSpace should successfully leave and refresh space details`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = listOf(currentUsername))
        val membershipResponse = SpaceMembershipResponse("Success", true)
        
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId))
            .thenReturn(flowOf(spaceDetails))
            .thenReturn(flowOf(spaceDetails.copy(collaborators = emptyList())))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        whenever(mockSpacesRepository.leaveSpace(testSpaceId))
            .thenReturn(Result.success(membershipResponse))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.leaveSpace()
        advanceUntilIdle()

        assertFalse(viewModel.isJoiningLeavingSpace.value)
        assertNull(viewModel.error.value)
        verify(mockSpacesRepository).leaveSpace(testSpaceId)
        verify(mockSpacesRepository, times(2)).getSpaceDetails(testSpaceId)
    }

    @Test
    fun `leaveSpace should set error on failure`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        
        val errorMessage = "Leave failed"
        whenever(mockSpacesRepository.leaveSpace(testSpaceId))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.leaveSpace()
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isJoiningLeavingSpace.value)
    }

    @Test
    fun `deleteSpace should set deleteSuccess to true on success`() = runTest {
        val spaceDetails = createTestSpaceDetails(creatorUsername = currentUsername)
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        whenever(mockSpacesRepository.deleteSpace(testSpaceId))
            .thenReturn(Result.success(Unit))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.deleteSpace()
        advanceUntilIdle()

        assertTrue(viewModel.deleteSuccess.value)
        assertFalse(viewModel.isDeletingSpace.value)
        verify(mockSpacesRepository).deleteSpace(testSpaceId)
    }

    @Test
    fun `deleteSpace should set error on failure`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        
        val errorMessage = "Delete failed"
        whenever(mockSpacesRepository.deleteSpace(testSpaceId))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.deleteSpace()
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isDeletingSpace.value)
        assertFalse(viewModel.deleteSuccess.value)
    }

    @Test
    fun `resetDeleteSuccess should reset deleteSuccess to false`() = runTest {
        val spaceDetails = createTestSpaceDetails(creatorUsername = currentUsername)
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        whenever(mockSpacesRepository.deleteSpace(testSpaceId))
            .thenReturn(Result.success(Unit))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.deleteSpace()
        advanceUntilIdle()
        assertTrue(viewModel.deleteSuccess.value)
        viewModel.resetDeleteSuccess()
        assertFalse(viewModel.deleteSuccess.value)
    }

    @Test
    fun `voteDiscussion should reject vote when user is not a collaborator`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = emptyList())
        val discussion = createTestDiscussion()
        
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(listOf(discussion)))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.voteDiscussion("1", "up")
        advanceUntilIdle()

        assertTrue(viewModel.voteRequiresCollaboratorError.value)
        verify(mockSpacesRepository, org.mockito.kotlin.never()).voteDiscussion(any(), any(), any())
    }

    @Test
    fun `voteDiscussion should allow vote when user is a collaborator`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = listOf(currentUsername))
        val discussion = createTestDiscussion(userReaction = null)
        val updatedDiscussionDto = createTestDiscussionDto(userReaction = "up", upvotes = 1)
        
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(listOf(discussion)))
        whenever(mockSpacesRepository.voteDiscussion(testSpaceId, "1", "up"))
            .thenReturn(Result.success(updatedDiscussionDto))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.voteDiscussion("1", "up")
        advanceUntilIdle()

        assertFalse(viewModel.voteRequiresCollaboratorError.value)
        val updatedDiscussion = viewModel.discussions.value.first()
        assertEquals("up", updatedDiscussion.userReaction)
        assertEquals(1, updatedDiscussion.upvotes)
    }

    @Test
    fun `voteDiscussion should toggle vote from down to up`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = listOf(currentUsername))
        val discussion = createTestDiscussion(userReaction = "down", upvotes = 0, downvotes = 1)
        val updatedDiscussionDto = createTestDiscussionDto(userReaction = "up", upvotes = 1, downvotes = 0)
        
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(listOf(discussion)))
        whenever(mockSpacesRepository.voteDiscussion(testSpaceId, "1", "up"))
            .thenReturn(Result.success(updatedDiscussionDto))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.voteDiscussion("1", "up")
        advanceUntilIdle()

        val updatedDiscussion = viewModel.discussions.value.first()
        assertEquals("up", updatedDiscussion.userReaction)
        assertEquals(1, updatedDiscussion.upvotes)
        assertEquals(0, updatedDiscussion.downvotes)
    }

    @Test
    fun `voteDiscussion should set error when discussion not found`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = listOf(currentUsername))
        
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.voteDiscussion("999", "up")
        advanceUntilIdle()

        assertEquals("Failed to vote on discussion", viewModel.error.value)
    }

    @Test
    fun `voteDiscussion should set error on repository failure`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = listOf(currentUsername))
        val discussion = createTestDiscussion()
        
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(listOf(discussion)))
        whenever(mockSpacesRepository.voteDiscussion(testSpaceId, "1", "up"))
            .thenReturn(Result.failure(Exception("Vote failed")))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.voteDiscussion("1", "up")
        advanceUntilIdle()

        assertEquals("Vote failed", viewModel.error.value)
    }

    @Test
    fun `getProfileByUsername should use me endpoint when viewing own profile`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        
        whenever(mockProfileRepository.getProfile(null)).thenReturn(
            flowOf(User(
                id = "1",
                username = currentUsername,
                profession = "test",
                bio = "test bio",
                dateOfBirth = "2000-01-01",
                joinedDate = "2023-01-01",
                ownedSpaces = emptyList(),
                joinedSpaces = emptyList()
            ))
        )

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.getProfileByUsername(currentUsername)
        advanceUntilIdle()

        assertEquals(currentUsername, viewModel.profileLoadSuccess.value)
        assertFalse(viewModel.isLoadingProfile.value)
        verify(mockProfileRepository).getProfile(null)
    }

    @Test
    fun `getProfileByUsername should use username endpoint when viewing other profile`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        val otherUsername = "otheruser"
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        
        whenever(mockProfileRepository.getProfile(otherUsername)).thenReturn(
            flowOf(User(
                id = "2",
                username = otherUsername,
                profession = "test",
                bio = "test bio",
                dateOfBirth = "2000-01-01",
                joinedDate = "2023-01-01",
                ownedSpaces = emptyList(),
                joinedSpaces = emptyList()
            ))
        )

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.getProfileByUsername(otherUsername)
        advanceUntilIdle()

        assertEquals(otherUsername, viewModel.profileLoadSuccess.value)
        assertFalse(viewModel.isLoadingProfile.value)
        verify(mockProfileRepository).getProfile(otherUsername)
    }

    @Test
    fun `getProfileByUsername should set error on failure`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        
        val errorMessage = "Profile fetch failed"
        whenever(mockProfileRepository.getProfile(any())).thenReturn(
            flow { throw Exception(errorMessage) }
        )

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.getProfileByUsername("someuser")
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoadingProfile.value)
        assertNull(viewModel.profileLoadSuccess.value)
    }

    @Test
    fun `resetProfileLoadSuccess should reset profileLoadSuccess to null`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()
        viewModel.resetProfileLoadSuccess()
        assertNull(viewModel.profileLoadSuccess.value)
    }

    @Test
    fun `clearError should reset error to null`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()
        viewModel.clearError()
        
        assertNull(viewModel.error.value)
    }

    @Test
    fun `clearVoteRequiresCollaboratorError should reset voteRequiresCollaboratorError to false`() = runTest {
        val spaceDetails = createTestSpaceDetails()
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()
        viewModel.clearVoteRequiresCollaboratorError()
        assertFalse(viewModel.voteRequiresCollaboratorError.value)
    }

    @Test
    fun `voteDiscussion should update correct discussion in list`() = runTest {
        val spaceDetails = createTestSpaceDetails(collaborators = listOf(currentUsername))
        val discussion1 = createTestDiscussion(id = 1, userReaction = null)
        val discussion2 = createTestDiscussion(id = 2, userReaction = null)
        val updatedDiscussionDto = createTestDiscussionDto(id = 2, userReaction = "up", upvotes = 1)
        
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(spaceDetails))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(listOf(discussion1, discussion2)))
        whenever(mockSpacesRepository.voteDiscussion(testSpaceId, "2", "up"))
            .thenReturn(Result.success(updatedDiscussionDto))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.voteDiscussion("2", "up")
        advanceUntilIdle()

        val discussions = viewModel.discussions.value
        assertEquals(2, discussions.size)
        assertEquals(null, discussions[0].userReaction)
        assertEquals("up", discussions[1].userReaction)
        assertEquals(1, discussions[1].upvotes)
    }

    @Test
    fun `isColorBlindTheme should reflect repository state`() = runTest {
        whenever(mockSpacesRepository.getSpaceDetails(testSpaceId)).thenReturn(flowOf(createTestSpaceDetails()))
        whenever(mockSpacesRepository.getSpaceDiscussions(testSpaceId)).thenReturn(flowOf(emptyList()))
        whenever(mockSpacesRepository.isColorBlindTheme).thenReturn(flowOf(true))

        viewModel = SpaceDetailsViewModel(
            mockContext,
            mockSpacesRepository,
            mockProfileRepository,
            mockUserPreferencesRepository,
            savedStateHandle
        )
        advanceUntilIdle()

        viewModel.isColorBlindTheme.test {
            assertTrue(awaitItem())
        }
    }
}

