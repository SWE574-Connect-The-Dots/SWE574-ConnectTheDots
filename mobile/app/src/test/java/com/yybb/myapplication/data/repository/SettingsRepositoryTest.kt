package com.yybb.myapplication.data.repository

import com.yybb.myapplication.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SettingsRepositoryTest {

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        userPreferencesRepository = mock()
        settingsRepository = SettingsRepository(userPreferencesRepository)
    }

    @Test
    fun `isColorBlindTheme should return flow from userPreferencesRepository`() = runTest {
        // Given
        val expectedFlow = flowOf(true)
        whenever(userPreferencesRepository.isColorBlindTheme).thenReturn(expectedFlow)

        // When
        val resultFlow = settingsRepository.isColorBlindTheme

        // Then
        assertEquals(expectedFlow.first(), resultFlow.first())
    }

    @Test
    fun `setColorBlindTheme should call userPreferencesRepository`() = runTest {
        // Given
        val isColorBlind = true

        // When
        settingsRepository.setColorBlindTheme(isColorBlind)

        // Then
        verify(userPreferencesRepository).setColorBlindTheme(isColorBlind)
    }
}
