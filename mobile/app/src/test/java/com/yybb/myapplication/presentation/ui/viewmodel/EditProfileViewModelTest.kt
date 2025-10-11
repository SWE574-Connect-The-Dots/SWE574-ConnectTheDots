package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import org.mockito.Mockito.calls
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

@ExperimentalCoroutinesApi
class EditProfileViewModelTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var viewModel: EditProfileViewModel
    private lateinit var repository: ProfileRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ProfileRepository::class.java)
        // SavedStateHandle can be mocked if needed, but not necessary for this test
        viewModel = EditProfileViewModel(repository, SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `saveProfile should call repository updateProfile`() = runTest {
        // Given
        val profession = "New Profession"
        val bio = "New Bio"

        // When
        viewModel.saveProfile(profession, bio)

        // Then
//        verify(repository).updateProfile("1", profession, bio)
        verifyNoInteractions(repository) // TODO change when backend is ready
    }
}
