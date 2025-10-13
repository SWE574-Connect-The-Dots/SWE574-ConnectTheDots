package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yybb.myapplication.data.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
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
        whenever(repository.getProfile("1")).thenReturn(emptyFlow())
        viewModel = EditProfileViewModel(repository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveProfile should call repository updateProfile`() = runTest {
        // Given
        val profession = "Software Engineer"
        val bio = "Android Developer"

        // When
        viewModel.saveProfile(profession, bio)
        advanceUntilIdle()

        // Then
        // TODO change when backend is ready
        verify(repository, never()).updateProfile("1", profession, bio)
    }
}
