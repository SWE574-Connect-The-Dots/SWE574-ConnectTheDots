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
    val dateOfBirth: String
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
    val dateOfBirth: String,
    @SerializedName("created_at")
    val joinedDate: String,
    @SerializedName("owned_spaces")
    val ownedSpaces: List<SpaceDto>,
    @SerializedName("joined_spaces")
    val joinedSpaces: List<SpaceDto>
)

data class SpaceDto(
    val id: Int,
    @SerializedName("title")
    val name: String,
    val description: String
)

data class UpdateProfileRequest(
    val bio: String?,
    val profession: String
)


data class TagDto(
    val id: String,
    val label: String,
    val description: String,
    val url: String
)
