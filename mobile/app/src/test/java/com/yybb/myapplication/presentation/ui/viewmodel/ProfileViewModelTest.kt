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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
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
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ProfileRepository::class.java)
        userPreferencesRepository = mock(UserPreferencesRepository::class.java)
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

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle)
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

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle)
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

        viewModel = ProfileViewModel(repository, userPreferencesRepository, savedStateHandle)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Error)
            assertEquals(errorMessage, (state as ProfileUiState.Error).message)
        }
    }
}
