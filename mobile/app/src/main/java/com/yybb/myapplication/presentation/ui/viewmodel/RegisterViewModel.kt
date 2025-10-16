package com.yybb.myapplication.presentation.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.presentation.ui.utils.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.yybb.myapplication.R
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _eventChannel = Channel<AuthEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    private val _viewState = MutableStateFlow<ViewState<Unit>>(ViewState.Success(Unit))
    val viewState: StateFlow<ViewState<Unit>> = _viewState

    fun onBackToLoginClicked() {
        viewModelScope.launch {
            _eventChannel.send(AuthEvent.NavigateToLogin)
        }
    }

    fun checkInputsAndNavigate(
        email: String,
        username: String,
        password: String,
        profession: String,
        dateOfBirth: String,
        agreeToShareLocation: Boolean
    ) {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading

            when {
                email.isBlank() || username.isBlank() || password.isBlank() ||
                        profession.isBlank() || dateOfBirth.isBlank() ->
                    _viewState.value = ViewState.Error(context.getString(R.string.fill_all_fileds_error))

                !isValidEmail(email) ->
                    _viewState.value = ViewState.Error(context.getString(R.string.invalid_email_error))

                !profession.matches(Regex("^[A-Za-z\\s]+\$")) ->
                    _viewState.value = ViewState.Error(context.getString(R.string.invalid_profession_error))

                calculateAge(dateOfBirth)?.let { it < 18 } ?: true ->
                    _viewState.value = ViewState.Error(context.getString(R.string.age_error))

                !agreeToShareLocation ->
                    _viewState.value = ViewState.Error(context.getString(R.string.consent_error))

                else -> {
                    _viewState.value = ViewState.Success(Unit)
                    _eventChannel.send(AuthEvent.NavigateToLogin)
                }
            }
        }
    }

    fun clearError() {
        _viewState.value = ViewState.Success(Unit)
    }

    private fun calculateAge(dateOfBirth: String): Int? {
        return try {
            val format = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            format.isLenient = false
            val birthDate = format.parse(dateOfBirth) ?: return null
            val today = Calendar.getInstance()
            val birth = Calendar.getInstance().apply { time = birthDate }

            var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }
}