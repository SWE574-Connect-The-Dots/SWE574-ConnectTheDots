package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yybb.myapplication.data.network.dto.CountryPosition
import com.yybb.myapplication.data.network.dto.TagDto
import com.yybb.myapplication.data.repository.CountriesRepository
import com.yybb.myapplication.data.repository.SpacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RetrieveTagWikidataUiState {
    object Loading : RetrieveTagWikidataUiState
    data class Success(val tags: List<TagDto>, val showResults: Boolean = false) : RetrieveTagWikidataUiState
    data class Error(val message: String) : RetrieveTagWikidataUiState
    object Initial : RetrieveTagWikidataUiState
}

sealed interface CreateSpaceUiState {
    object Loading : CreateSpaceUiState
    data class Success(val spaceId: Int) : CreateSpaceUiState
    data class Error(val message: String) : CreateSpaceUiState
    object Initial : CreateSpaceUiState
}

@Stable
data class CreateSpaceFormState(
    val spaceTitle: String = "",
    val spaceDescription: String = "",
    val selectedTags: List<TagDto> = emptyList(),
    val showTagSearch: Boolean = false,
    var tagSearchQuery: String = "",
    var showSearchResults: Boolean = false
) {
    val isFormValid: Boolean
        get() = spaceTitle.isNotBlank() && spaceDescription.isNotBlank()

    val isSearchEnabled: Boolean
        get() = tagSearchQuery.isNotBlank()
}

@HiltViewModel
class CreateSpaceViewModel @Inject constructor(
    private val spaceRepository: SpacesRepository,
    private val countriesRepository: CountriesRepository
) : ViewModel() {

    private val _tagWikidataUiState = MutableStateFlow<RetrieveTagWikidataUiState>(RetrieveTagWikidataUiState.Initial)
    val tagWikidataUiState: StateFlow<RetrieveTagWikidataUiState> = _tagWikidataUiState.asStateFlow()

    private val _createSpaceUiState = MutableStateFlow<CreateSpaceUiState>(CreateSpaceUiState.Initial)
    val createSpaceUiState: StateFlow<CreateSpaceUiState> = _createSpaceUiState.asStateFlow()

    private val _formState = MutableStateFlow(CreateSpaceFormState())
    val formState: StateFlow<CreateSpaceFormState> = _formState.asStateFlow()

    private val _isColorBlindTheme = MutableStateFlow(false)
    val isColorBlindTheme: StateFlow<Boolean> = _isColorBlindTheme.asStateFlow()

    // Location state
    private val _showLocationDialog = MutableStateFlow(false)
    val showLocationDialog: StateFlow<Boolean> = _showLocationDialog.asStateFlow()

    private val _countries = MutableStateFlow<List<CountryPosition>>(emptyList())
    val countries: StateFlow<List<CountryPosition>> = _countries.asStateFlow()

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities: StateFlow<List<String>> = _cities.asStateFlow()

    private val _isLoadingCountries = MutableStateFlow(false)
    val isLoadingCountries: StateFlow<Boolean> = _isLoadingCountries.asStateFlow()

    private val _isLoadingCities = MutableStateFlow(false)
    val isLoadingCities: StateFlow<Boolean> = _isLoadingCities.asStateFlow()

    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry: StateFlow<String?> = _selectedCountry.asStateFlow()

    private val _selectedCity = MutableStateFlow<String?>(null)
    val selectedCity: StateFlow<String?> = _selectedCity.asStateFlow()

    init {
        spaceRepository.isColorBlindTheme
            .onEach { _isColorBlindTheme.value = it }
            .launchIn(viewModelScope)
        loadCountries()
    }

    fun getTags(tagQuery: String) {
        viewModelScope.launch {
            _tagWikidataUiState.value = RetrieveTagWikidataUiState.Loading
            spaceRepository.getWikiTags(tagQuery)
                .onSuccess {
                    _tagWikidataUiState.value = RetrieveTagWikidataUiState.Success(it, showResults = true)
                }
                .onFailure {
                    _tagWikidataUiState.value = RetrieveTagWikidataUiState.Error(it.message ?: "An unknown error occurred")
                }
        }
    }

    fun updateFormState(newState: CreateSpaceFormState) {
        if (newState.showTagSearch != _formState.value.showTagSearch) {
            if (newState.showTagSearch == false) {
                newState.showSearchResults = false
                newState.tagSearchQuery = ""
            }
        }
        _formState.value = newState
    }

    fun resetFormState() {
        _formState.value = CreateSpaceFormState()
    }

    fun resetTagWikidataState() {
        _tagWikidataUiState.value = RetrieveTagWikidataUiState.Initial
    }

    fun createSpace(_formState: CreateSpaceFormState) {
        viewModelScope.launch {
            _createSpaceUiState.value = CreateSpaceUiState.Loading
            spaceRepository.createSpace(
                _formState.spaceTitle,
                _formState.spaceDescription,
                _formState.selectedTags,
                _selectedCountry.value,
                _selectedCity.value
            )
                .onSuccess {
                    resetFormState()
                    _createSpaceUiState.value = CreateSpaceUiState.Success(spaceId = it.id)
                }
                .onFailure {
                    _createSpaceUiState.value = CreateSpaceUiState.Error(it.message ?: "An unknown error occurred")
                }
        }
    }

    // Location functions
    fun showLocationDialog() {
        _showLocationDialog.value = true
    }

    fun hideLocationDialog() {
        _showLocationDialog.value = false
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

    fun saveLocationData(country: String?, city: String?) {
        _selectedCountry.value = country
        _selectedCity.value = city
        _showLocationDialog.value = false
    }

    fun resetCreateSpaceState() {
        _createSpaceUiState.value = CreateSpaceUiState.Initial
    }
}