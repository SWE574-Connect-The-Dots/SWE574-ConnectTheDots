package com.yybb.myapplication.presentation.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.network.dto.CountryPosition
import com.yybb.myapplication.data.network.dto.RegisterRequest
import com.yybb.myapplication.data.repository.AuthRepository
import com.yybb.myapplication.data.repository.CountriesRepository
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
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val countriesRepository: CountriesRepository
) : ViewModel() {

    private val _eventChannel = Channel<AuthEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    private val _viewState = MutableStateFlow<ViewState<Unit>>(ViewState.Success(Unit))
    val viewState: StateFlow<ViewState<Unit>> = _viewState

    private val _countries = MutableStateFlow<List<CountryPosition>>(emptyList())
    val countries: StateFlow<List<CountryPosition>> = _countries

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities: StateFlow<List<String>> = _cities

    private val _isLoadingCountries = MutableStateFlow(false)
    val isLoadingCountries: StateFlow<Boolean> = _isLoadingCountries

    private val _isLoadingCities = MutableStateFlow(false)
    val isLoadingCities: StateFlow<Boolean> = _isLoadingCities

    init {
        loadCountries()
    }

    fun loadCountries() {
        viewModelScope.launch {
            _isLoadingCountries.value = true
            countriesRepository.getCountries()
                .onSuccess { countriesList ->
                    _countries.value = (countriesList ?: emptyList()).sortedBy { it.name }
                    _isLoadingCountries.value = false
                }
                .onFailure {
                    _isLoadingCountries.value = false
                }
        }
    }

    fun loadCities(country: String) {
        viewModelScope.launch {
            _isLoadingCities.value = true
            _cities.value = emptyList()
            countriesRepository.getCities(country)
                .onSuccess { citiesList ->
                    _cities.value = citiesList.sorted()
                    _isLoadingCities.value = false
                }
                .onFailure {
                    _isLoadingCities.value = false
                }
        }
    }

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
        country: String?,
        city: String?
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

                country.isNullOrBlank() ->
                    _viewState.value = ViewState.Error("Please select a country")

                city.isNullOrBlank() ->
                    _viewState.value = ViewState.Error("Please select a city")

                else -> {
                    val locationName = "$city, $country"
                    val selectedCountry = _countries.value.find { it.name == country }
                    val latitude = selectedCountry?.lat?.let {
                        when (it) {
                            is Number -> it.toDouble()
                            is String -> it.toDoubleOrNull()
                            else -> null
                        }
                    }
                    val longitude = selectedCountry?.long?.let {
                        when (it) {
                            is Number -> it.toDouble()
                            is String -> it.toDoubleOrNull()
                            else -> null
                        }
                    }

                    val registerRequest = RegisterRequest(
                        email = email,
                        username = username,
                        password = password,
                        profession = profession,
                        dateOfBirth = dateOfBirth,
                        city = city,
                        country = country,
                        locationName = locationName,
                        latitude = latitude,
                        longitude = longitude
                    )
                    authRepository.register(registerRequest)
                        .onSuccess {
                            _viewState.value = ViewState.Success(Unit)
                            _eventChannel.send(AuthEvent.NavigateToLogin)
                        }
                        .onFailure {
                            _viewState.value = ViewState.Error(it.message ?: "An unknown error occurred")
                        }
                }
            }
        }
    }

    fun clearError() {
        _viewState.value = ViewState.Success(Unit)
    }

    private fun calculateAge(dateOfBirth: String): Int? {
        return try {
            val format = SimpleDateFormat("yyyy-mm-dd", Locale.getDefault())
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