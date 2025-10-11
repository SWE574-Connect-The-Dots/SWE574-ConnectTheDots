package com.yybb.myapplication.presentation.ui.viewmodel

import com.yybb.myapplication.data.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var repository: ProfileRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ProfileRepository::class.java)
        viewModel = ProfileViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `getProfile should call repository`() = runTest {
        // When
        // init block calls getProfile("1")

        // Then
        verify(repository).getProfile("1")
    }
}
