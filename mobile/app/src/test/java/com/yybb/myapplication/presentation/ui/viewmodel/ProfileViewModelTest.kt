package com.yybb.myapplication.presentation.ui.viewmodel

import app.cash.turbine.test
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var repository: ProfileRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ProfileRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProfile should call repository`() = runTest {
        //Given
        whenever(repository.getProfile("1")).thenReturn(emptyFlow())

        // When
        viewModel = ProfileViewModel(repository)
        advanceUntilIdle()

        // Then
        verify(repository).getProfile("1")
    }
}
