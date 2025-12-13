package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.network.dto.CountryPosition
import com.yybb.myapplication.data.repository.CountriesRepository
import com.yybb.myapplication.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EditProfileUiState {
    object Loading : EditProfileUiState
    data class Success(val user: User) : EditProfileUiState
    data class Error(val message: String) : EditProfileUiState
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val countriesRepository: CountriesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _countries = MutableStateFlow<List<CountryPosition>>(emptyList())
    val countries: StateFlow<List<CountryPosition>> = _countries

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities: StateFlow<List<String>> = _cities

    private val _isLoadingCountries = MutableStateFlow(false)
    val isLoadingCountries: StateFlow<Boolean> = _isLoadingCountries

    private val _isLoadingCities = MutableStateFlow(false)
    val isLoadingCities: StateFlow<Boolean> = _isLoadingCities

    init {
        getProfile()
        loadCountries()
    }

    private fun getProfile() {
        repository.getProfile(null).onEach { user ->
            _uiState.value = EditProfileUiState.Success(user)
        }.catch { e ->
            _uiState.value = EditProfileUiState.Error(e.message ?: "An unknown error occurred")
        }.launchIn(viewModelScope)
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

    fun saveProfile(
        profession: String, 
        bio: String?,
        city: String?,
        country: String?
    ) {
        viewModelScope.launch {
            val locationName = if (city != null && country != null) {
                "$city, $country"
            } else {
                null
            }
            
            repository.updateProfile(profession, bio, city, country, locationName)
                .onSuccess {
                    _uiState.value = EditProfileUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = EditProfileUiState.Error(it.message ?: "An unknown error occurred")
                }
        }
    }
}
