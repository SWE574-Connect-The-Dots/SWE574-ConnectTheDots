package com.yybb.myapplication.presentation.ui.viewmodel

import app.cash.turbine.test
import com.yybb.myapplication.data.repository.AuthRepository
import com.yybb.myapplication.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mock()
        authRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isColorBlindTheme should have initial value of false`() = runTest {
        // Given
        whenever(settingsRepository.isColorBlindTheme).thenReturn(flowOf(false))
        viewModel = SettingsViewModel(settingsRepository, authRepository)

        // Then
        viewModel.isColorBlindTheme.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `isColorBlindTheme should update when repository emits new value`() = runTest {
        // Given
        whenever(settingsRepository.isColorBlindTheme).thenReturn(flowOf(false, true))
        viewModel = SettingsViewModel(settingsRepository, authRepository)


        // Then
        viewModel.isColorBlindTheme.test {
            assertEquals(false, awaitItem())
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `setColorBlindTheme should call repository`() = runTest {
        // Given
        whenever(settingsRepository.isColorBlindTheme).thenReturn(flowOf(false))
        viewModel = SettingsViewModel(settingsRepository, authRepository)
        val isColorBlind = true

        // When
        viewModel.setColorBlindTheme(isColorBlind)
        advanceUntilIdle()

        // Then
        verify(settingsRepository).setColorBlindTheme(isColorBlind)
    }

    @Test
    fun `logout should call authRepository and emit logout event`() = runTest {
        // Given
        whenever(settingsRepository.isColorBlindTheme).thenReturn(flowOf(false))
        viewModel = SettingsViewModel(settingsRepository, authRepository)

        // Then
        viewModel.logoutEvent.test {
            viewModel.logout()
            advanceUntilIdle()
            verify(authRepository).logout()
            assertEquals(Unit, awaitItem())
        }
    }
}
