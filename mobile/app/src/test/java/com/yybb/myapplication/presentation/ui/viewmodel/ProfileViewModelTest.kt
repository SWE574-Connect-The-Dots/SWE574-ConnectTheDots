package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.repository.ProfileRepository
import junit.framework.TestCase.assertEquals
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
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var repository: ProfileRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ProfileRepository::class.java)
        savedStateHandle = mock(SavedStateHandle::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProfile should update uiState to Success`() = runTest {
        val user = User("1", "test", "test", "test", "test", "test", emptyList(), emptyList())
        whenever(savedStateHandle.get<String>("userId")).thenReturn("1")
        whenever(repository.getProfile("1")).thenReturn(flowOf(user))

        viewModel = ProfileViewModel(repository, savedStateHandle)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Success)
            assertEquals(user, (state as ProfileUiState.Success).user)
        }
    }

    @Test
    fun `getProfile should update uiState to Error on failure`() = runTest {
        val errorMessage = "Error fetching profile"
        whenever(savedStateHandle.get<String>("userId")).thenReturn("1")
        whenever(repository.getProfile("1")).thenReturn(flow { throw Exception(errorMessage) })

        viewModel = ProfileViewModel(repository, savedStateHandle)
        viewModel.getProfile()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Error)
            assertEquals(errorMessage, (state as ProfileUiState.Error).message)
        }
    }
}
