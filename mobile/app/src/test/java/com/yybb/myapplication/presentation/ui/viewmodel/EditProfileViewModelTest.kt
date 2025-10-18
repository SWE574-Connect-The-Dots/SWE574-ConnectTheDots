package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.repository.ProfileRepository
import junit.framework.TestCase
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

@ExperimentalCoroutinesApi
class EditProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: EditProfileViewModel
    private lateinit var repository: ProfileRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        savedStateHandle = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should update uiState to Success`() = runTest {
        val user = User("1", "test", "test", "test", "test", "test", emptyList(), emptyList())
        whenever(repository.getProfile(null)).thenReturn(flowOf(user))

        viewModel = EditProfileViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            TestCase.assertTrue(state is EditProfileUiState.Success)
            TestCase.assertEquals(user, (state as EditProfileUiState.Success).user)
        }
    }

    @Test
    fun `saveProfile should call repository updateProfile`() = runTest {
        // Given
        val profession = "Software Engineer"
        val bio = "Android Developer"
        val user = User("1", "test", "test", "test", "test", "test", emptyList(), emptyList())
        whenever(repository.getProfile(null)).thenReturn(flowOf(user))
        whenever(repository.updateProfile(profession, bio)).thenReturn(Result.success(user))
        viewModel = EditProfileViewModel(repository, savedStateHandle)
        advanceUntilIdle()


        // When
        viewModel.saveProfile(profession, bio)
        advanceUntilIdle()

        // Then
        verify(repository).updateProfile(profession, bio)
    }
}
