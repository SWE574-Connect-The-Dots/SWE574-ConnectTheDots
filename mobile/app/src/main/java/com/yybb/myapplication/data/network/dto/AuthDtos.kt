package com.yybb.myapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val profession: String,
    @SerializedName("dob")
    val dateOfBirth: String,
    val city: String? = null,
    val country: String? = null,
    @SerializedName("location_name")
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class UserDto(
    val id: Int,
    val username: String,
    val email: String
)

data class ProfileResponse(
    val user: UserDto,
    val bio: String?,
    val profession: String,
    @SerializedName("dob")
    val dateOfBirth: String?,
    @SerializedName("created_at")
    val joinedDate: String,
    @SerializedName("owned_spaces")
    val ownedSpaces: List<SpaceDto>,
    @SerializedName("joined_spaces")
    val joinedSpaces: List<SpaceDto>,
    val country: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("location_name")
    val locationName: String? = null
)

data class UpdateProfileRequest(
    val bio: String?,
    val profession: String,
    val city: String? = null,
    val country: String? = null,
    @SerializedName("location_name")
    val locationName: String? = null
)
