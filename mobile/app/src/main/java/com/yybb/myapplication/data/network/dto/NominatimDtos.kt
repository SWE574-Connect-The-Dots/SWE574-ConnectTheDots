package com.yybb.myapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class NominatimResponse(
    @SerializedName("place_id")
    val placeId: Long,
    val licence: String,
    @SerializedName("osm_type")
    val osmType: String,
    @SerializedName("osm_id")
    val osmId: Long,
    val lat: String,
    val lon: String,
    val `class`: String,
    val type: String,
    @SerializedName("place_rank")
    val placeRank: Int,
    val importance: Double,
    @SerializedName("addresstype")
    val addressType: String,
    val name: String,
    @SerializedName("display_name")
    val displayName: String,
    val address: NominatimAddress?,
    val boundingbox: List<String>?
)

data class NominatimAddress(
    val city: String?,
    @SerializedName("state_district")
    val stateDistrict: String?,
    val state: String?,
    @SerializedName("ISO3166-2-lvl4")
    val iso3166: String?,
    val country: String?,
    @SerializedName("country_code")
    val countryCode: String?
)

data class NominatimCoordinates(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)


