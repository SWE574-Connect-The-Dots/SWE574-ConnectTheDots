package com.yybb.myapplication.presentation.ui.viewmodel

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.ui.utils.ViewState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: LoginViewModel
    private lateinit var mockContext: Context


    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockContext = mock()
        whenever(mockContext.getString(R.string.fill_all_fileds_error)).thenReturn("Please fill all fields")
        viewModel = LoginViewModel(mockContext)
    }

    @Test
    fun `onRegisterClicked should emit NavigateToRegister event`() = runTest {
        viewModel.onRegisterClicked()

        val event = viewModel.eventFlow.first()
        assertTrue(event is AuthEvent.NavigateToRegister)
    }

    @Test
    fun `onLoginClicked with blank username or password should set ViewState_Error`() = runTest {
        viewModel.onLoginClicked("", "")
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertTrue(state is ViewState.Error)
        assertEquals("Please fill all fields", (state as ViewState.Error).message)
    }

    @Test
    fun `onLoginClicked with blank password should set ViewState_Error`() = runTest {
        viewModel.onLoginClicked("johnDoe", "")
        advanceUntilIdle()

        val state = viewModel.viewState.first()
        assertTrue(state is ViewState.Error)
        assertEquals("Please fill all fields", (state as ViewState.Error).message)
    }

    @Test
    fun `onLoginClicked with blank username should set ViewState_Error`() = runTest {
        viewModel.onLoginClicked("", "john1234")
        advanceUntilIdle()

        val state = viewModel.viewState.first()
        assertTrue(state is ViewState.Error)
        assertEquals("Please fill all fields", (state as ViewState.Error).message)
    }


    @Test
    fun `clearError should reset ViewState to Success`() = runTest {
        viewModel.onLoginClicked("", "")
        advanceUntilIdle()

        assertTrue(viewModel.viewState.first() is ViewState.Error)
        viewModel.clearError()
        assertTrue(viewModel.viewState.first() is ViewState.Success)
    }

    @Test
    fun `onLoginClicked with valid credentials should emit NavigateToMain event`() = runTest {
        viewModel.onLoginClicked("johnDoe", "john1234")

        val event = viewModel.eventFlow.first()
        assertTrue(event is AuthEvent.NavigateToMain)

        val state = viewModel.viewState.first()
        assertTrue(state is ViewState.Success)
    }
}
