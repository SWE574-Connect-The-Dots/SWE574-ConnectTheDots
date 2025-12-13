package com.yybb.myapplication.data.model

data class User(
    val id: String,
    val username: String,
    val profession: String,
    val bio: String?,
    val dateOfBirth: String?,
    val joinedDate: String,
    val ownedSpaces: List<Space> = emptyList(),
    val joinedSpaces: List<Space> = emptyList(),
    val locationName: String? = null
)
