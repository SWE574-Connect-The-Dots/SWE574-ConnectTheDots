package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.repository.CountriesRepository
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
    private lateinit var mockCountriesRepository: CountriesRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        mockCountriesRepository = mock()
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

        viewModel = EditProfileViewModel(repository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            TestCase.assertTrue(state is EditProfileUiState.Success)
            TestCase.assertEquals(user, (state as EditProfileUiState.Success).user)
        }
    }

    @Test
    fun `saveProfile should call repository updateProfile`() = runTest {
        val profession = "Software Engineer"
        val bio = "Android Developer"
        val city: String? = null
        val country: String? = null
        val user = User("1", "test", "test", "test", "test", "test", emptyList(), emptyList())
        whenever(repository.getProfile(null)).thenReturn(flowOf(user))
        whenever(repository.updateProfile(profession, bio, city, country, null)).thenReturn(Result.success(user))
        viewModel = EditProfileViewModel(repository, mockCountriesRepository, savedStateHandle)
        advanceUntilIdle()


        viewModel.saveProfile(profession, bio, city, country)
        advanceUntilIdle()

        verify(repository).updateProfile(profession, bio, city, country, null)
    }
}
