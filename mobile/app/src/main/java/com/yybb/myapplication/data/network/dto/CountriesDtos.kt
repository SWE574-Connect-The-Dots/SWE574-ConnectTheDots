package com.yybb.myapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class CountryPosition(
    val name: String,
    val iso2: String,
    val long: Any, // Can be String or Number
    val lat: Any  // Can be String or Number
)

data class CountriesResponse(
    val error: Boolean,
    val msg: String,
    val data: List<CountryPosition>
)

data class CitiesResponse(
    val error: Boolean,
    val msg: String,
    val data: List<String>
)

