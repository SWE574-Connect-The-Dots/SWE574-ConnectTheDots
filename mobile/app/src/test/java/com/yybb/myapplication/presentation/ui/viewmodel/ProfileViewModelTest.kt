package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.repository.ProfileRepository
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
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
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var repository: ProfileRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var spacesRepository: com.yybb.myapplication.data.repository.SpacesRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ProfileRepository::class.java)
        userPreferencesRepository = mock(UserPreferencesRepository::class.java)
        spacesRepository = mock(com.yybb.myapplication.data.repository.SpacesRepository::class.java)
        savedStateHandle = mock(SavedStateHandle::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProfile should update uiState to Success when viewing other user profile`() = runTest {
        val username = "otheruser"
        val currentUsername = "testuser"
        val user = User("1", username, "test", "test", "test", "test", emptyList(), emptyList())
        
        whenever(savedStateHandle.get<String>("username")).thenReturn(username)
        whenever(userPreferencesRepository.username).thenReturn(flowOf(currentUsername))
        whenever(repository.getProfile(username)).thenReturn(flowOf(user))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Success)
            val successState = state as ProfileUiState.Success
            assertEquals(user, successState.user)
            TestCase.assertFalse(successState.isCurrentUser)
        }
    }

    @Test
    fun `getProfile should update uiState to Success when viewing own profile`() = runTest {
        val currentUsername = "testuser"
        val user = User("1", currentUsername, "test", "test", "test", "test", emptyList(), emptyList())
        
        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf(currentUsername))
        whenever(repository.getProfile(null)).thenReturn(flowOf(user))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Success)
            val successState = state as ProfileUiState.Success
            assertEquals(user, successState.user)
            assertTrue(successState.isCurrentUser)
        }
    }

    @Test
    fun `getProfile should update uiState to Error on failure`() = runTest {
        val errorMessage = "Error fetching profile"
        val currentUsername = "testuser"
        
        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf(currentUsername))
        whenever(repository.getProfile(null)).thenReturn(flow { throw Exception(errorMessage) })

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Error)
            assertEquals(errorMessage, (state as ProfileUiState.Error).message)
        }
    }

    @Test
    fun `fetchReportReasons should load profile report reasons successfully`() = runTest {
        val reportReasons = listOf(
            com.yybb.myapplication.data.network.dto.ReportReasonItem("INAPPROPRIATE", "Inappropriate content"),
            com.yybb.myapplication.data.network.dto.ReportReasonItem("FAKE_ACCOUNT", "Fake account")
        )

        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf("testuser"))
        whenever(repository.getProfile(null)).thenReturn(flowOf(
            User("1", "testuser", "test", "test", null, "test", emptyList(), emptyList())
        ))
        whenever(spacesRepository.getReportReasons("profile"))
            .thenReturn(Result.success(reportReasons))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.fetchReportReasons("profile")
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingReportReasons.value)
        assertEquals(reportReasons, viewModel.reportReasons.value)
        assertNull(viewModel.reportError.value)
    }

    @Test
    fun `fetchReportReasons should set error on failure`() = runTest {
        val errorMessage = "Failed to load report reasons"

        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf("testuser"))
        whenever(repository.getProfile(null)).thenReturn(flowOf(
            User("1", "testuser", "test", "test", null, "test", emptyList(), emptyList())
        ))
        whenever(spacesRepository.getReportReasons("profile"))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.fetchReportReasons("profile")
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingReportReasons.value)
        assertTrue(viewModel.reportReasons.value.isEmpty())
        assertEquals(errorMessage, viewModel.reportError.value)
    }

    @Test
    fun `prepareReport should set content type and ID and fetch reasons`() = runTest {
        val reportReasons = listOf(
            com.yybb.myapplication.data.network.dto.ReportReasonItem("INAPPROPRIATE", "Inappropriate content")
        )

        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf("testuser"))
        whenever(repository.getProfile(null)).thenReturn(flowOf(
            User("1", "testuser", "test", "test", null, "test", emptyList(), emptyList())
        ))
        whenever(spacesRepository.getReportReasons("profile"))
            .thenReturn(Result.success(reportReasons))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.prepareReport("profile", 123)
        advanceUntilIdle()

        assertEquals(reportReasons, viewModel.reportReasons.value)
        org.mockito.kotlin.verify(spacesRepository).getReportReasons("profile")
    }

    @Test
    fun `submitReport should submit profile report successfully`() = runTest {
        val submitResponse = com.yybb.myapplication.data.network.dto.SubmitReportResponse(
            id = 1,
            contentType = "profile",
            contentId = 123,
            reason = "INAPPROPRIATE",
            status = "OPEN",
            space = null,
            reporter = 1,
            reporterUsername = "testuser",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            entityReportCount = 1,
            entityIsReported = true
        )

        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf("testuser"))
        whenever(repository.getProfile(null)).thenReturn(flowOf(
            User("1", "testuser", "test", "test", null, "test", emptyList(), emptyList())
        ))
        whenever(spacesRepository.submitReport("profile", 123, "INAPPROPRIATE"))
            .thenReturn(Result.success(submitResponse))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.prepareReport("profile", 123)
        advanceUntilIdle()

        viewModel.submitReport("INAPPROPRIATE")
        advanceUntilIdle()

        assertFalse(viewModel.isSubmittingReport.value)
        assertTrue(viewModel.reportSubmitSuccess.value)
        assertNull(viewModel.reportError.value)
    }

    @Test
    fun `submitReport should set error on failure`() = runTest {
        val errorMessage = "Failed to submit report"

        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf("testuser"))
        whenever(repository.getProfile(null)).thenReturn(flowOf(
            User("1", "testuser", "test", "test", null, "test", emptyList(), emptyList())
        ))
        whenever(spacesRepository.submitReport("profile", 123, "INAPPROPRIATE"))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.prepareReport("profile", 123)
        advanceUntilIdle()

        viewModel.submitReport("INAPPROPRIATE")
        advanceUntilIdle()

        assertFalse(viewModel.isSubmittingReport.value)
        assertFalse(viewModel.reportSubmitSuccess.value)
        assertEquals(errorMessage, viewModel.reportError.value)
    }

    @Test
    fun `resetReportSubmitSuccess should reset success flag`() = runTest {
        val submitResponse = com.yybb.myapplication.data.network.dto.SubmitReportResponse(
            id = 1,
            contentType = "profile",
            contentId = 123,
            reason = "INAPPROPRIATE",
            status = "OPEN",
            space = null,
            reporter = 1,
            reporterUsername = "testuser",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            entityReportCount = 1,
            entityIsReported = true
        )

        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf("testuser"))
        whenever(repository.getProfile(null)).thenReturn(flowOf(
            User("1", "testuser", "test", "test", null, "test", emptyList(), emptyList())
        ))
        whenever(spacesRepository.submitReport("profile", 123, "INAPPROPRIATE"))
            .thenReturn(Result.success(submitResponse))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.prepareReport("profile", 123)
        advanceUntilIdle()
        viewModel.submitReport("INAPPROPRIATE")
        advanceUntilIdle()

        assertTrue(viewModel.reportSubmitSuccess.value)
        viewModel.resetReportSubmitSuccess()
        assertFalse(viewModel.reportSubmitSuccess.value)
    }

    @Test
    fun `clearReportError should clear error`() = runTest {
        val errorMessage = "Failed to submit report"

        whenever(savedStateHandle.get<String>("username")).thenReturn(null)
        whenever(userPreferencesRepository.username).thenReturn(flowOf("testuser"))
        whenever(repository.getProfile(null)).thenReturn(flowOf(
            User("1", "testuser", "test", "test", null, "test", emptyList(), emptyList())
        ))
        whenever(spacesRepository.submitReport("profile", 123, "INAPPROPRIATE"))
            .thenReturn(Result.failure(Exception(errorMessage)))

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle, spacesRepository)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.prepareReport("profile", 123)
        advanceUntilIdle()

        viewModel.submitReport("INAPPROPRIATE")
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.reportError.value)
        viewModel.clearReportError()
        assertNull(viewModel.reportError.value)
    }
}
