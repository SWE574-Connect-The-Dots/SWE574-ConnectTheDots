package com.yybb.myapplication.presentation.ui.viewmodel

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.data.repository.AuthRepository
import com.yybb.myapplication.data.repository.CountriesRepository
import com.yybb.myapplication.presentation.ui.utils.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockContext: Context
    private lateinit var viewModel: RegisterViewModel
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockCountriesRepository: CountriesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        MockitoAnnotations.openMocks(this)
        mockAuthRepository = Mockito.mock(AuthRepository::class.java)
        mockCountriesRepository = Mockito.mock(CountriesRepository::class.java)
        mockContext.apply {
            Mockito.`when`(getString(R.string.fill_all_fileds_error))
                .thenReturn("Please fill all fields")
            Mockito.`when`(getString(R.string.invalid_email_error)).thenReturn("Invalid email")
            Mockito.`when`(getString(R.string.invalid_profession_error))
                .thenReturn("Invalid profession")
            Mockito.`when`(getString(R.string.age_error)).thenReturn("Must be 18+")
            Mockito.`when`(getString(R.string.consent_error)).thenReturn("Consent required")
        }

        viewModel = RegisterViewModel(mockContext, mockAuthRepository, mockCountriesRepository)
    }

    @Test
    fun `checkInputsAndNavigate with at least one blank fields should set ViewState_Error`() =
        runTest(dispatcher) {
            viewModel.checkInputsAndNavigate("", "", "", "", "", null, null)
            advanceUntilIdle()

            val state = viewModel.viewState.value
            assertTrue(state is ViewState.Error)
            assertEquals("Please fill all fields", (state as ViewState.Error).message)
        }

    @Test
    fun `checkInputsAndNavigate with invalid email should set ViewState_Error`() =
        runTest(dispatcher) {
            viewModel.checkInputsAndNavigate(
                email = "invalidEmail.com",
                username = "johnDoe",
                password = "john1234",
                profession = "Engineer",
                dateOfBirth = "01/01/2000",
                country = "Turkey",
                city = "Istanbul"
            )
            advanceUntilIdle()

            val state = viewModel.viewState.value
            assertTrue(state is ViewState.Error)
            assertEquals("Invalid email", (state as ViewState.Error).message)
        }

    @Test
    fun `checkInputsAndNavigate with invalid profession should set ViewState_Error`() =
        runTest(dispatcher) {
            viewModel.checkInputsAndNavigate(
                email = "john@example.com",
                username = "johnDoe",
                password = "john1234",
                profession = "123Engineer!",
                dateOfBirth = "01/01/2000",
                country = "Turkey",
                city = "Istanbul"
            )
            advanceUntilIdle()

            val state = viewModel.viewState.value
            assertTrue(state is ViewState.Error)
            assertEquals("Invalid profession", (state as ViewState.Error).message)
        }

    @Test
    fun `checkInputsAndNavigate with underage date should set ViewState_Error`() =
        runTest(dispatcher) {
            // less than 18 years old
            viewModel.checkInputsAndNavigate(
                email = "john@example.com",
                username = "johnDoe",
                password = "john1234",
                profession = "Engineer",
                dateOfBirth = "01/01/2015",
                country = "Turkey",
                city = "Istanbul"
            )
            advanceUntilIdle()

            val state = viewModel.viewState.value
            assertTrue(state is ViewState.Error)
            assertEquals("Must be 18+", (state as ViewState.Error).message)
        }

    @Test
    fun `checkInputsAndNavigate without country should set ViewState_Error`() =
        runTest(dispatcher) {
            viewModel.checkInputsAndNavigate(
                email = "john@example.com",
                username = "johnDoe",
                password = "john1234",
                profession = "Engineer",
                dateOfBirth = "2000-01-01",
                country = null,
                city = "Istanbul"
            )
            advanceUntilIdle()

            val state = viewModel.viewState.value
            assertTrue(state is ViewState.Error)
            assertEquals("Please select a country", (state as ViewState.Error).message)
        }
    @Test
    fun `checkInputsAndNavigate with valid inputs should emit NavigateToLogin event`() = runTest(dispatcher) {
        whenever(mockAuthRepository.register(any())).thenReturn(Result.success(Unit))
        val job = launch { viewModel.eventFlow.first { it is AuthEvent.NavigateToLogin } }

        viewModel.checkInputsAndNavigate(
            email = "john@example.com",
            username = "johnDoe",
            password = "john1234",
            profession = "Engineer",
            dateOfBirth = "2000-01-01",
            country = "Turkey",
            city = "Istanbul"
        )
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertTrue(state is ViewState.Success)

        job.cancel()
    }

    @Test
    fun `checkInputsAndNavigate with valid inputs should set ViewState_Error on failure`() = runTest(dispatcher) {
        val errorMessage = "Registration failed"
        whenever(mockAuthRepository.register(any())).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.checkInputsAndNavigate(
            email = "john@example.com",
            username = "johnDoe",
            password = "john1234",
            profession = "Engineer",
            dateOfBirth = "2000-01-01",
            country = "Turkey",
            city = "Istanbul"
        )
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertTrue(state is ViewState.Error)
        assertEquals(errorMessage, (state as ViewState.Error).message)
    }

    @Test
    fun `onBackToLoginClicked should emit NavigateToLogin`() = runTest(dispatcher) {
        val job = launch { viewModel.eventFlow.first { it is AuthEvent.NavigateToLogin } }

        viewModel.onBackToLoginClicked()
        advanceUntilIdle()

        job.cancel()
    }

    @Test
    fun `clearError should reset ViewState to Success`() = runTest(dispatcher) {
        viewModel.checkInputsAndNavigate("", "", "", "", "", null, null)
        advanceUntilIdle()
        assertTrue(viewModel.viewState.value is ViewState.Error)

        viewModel.clearError()
        assertTrue(viewModel.viewState.value is ViewState.Success)
    }
}
