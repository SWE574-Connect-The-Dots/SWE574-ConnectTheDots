package com.yybb.myapplication.data.repository

import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.model.Space
import com.yybb.myapplication.data.model.User
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.ProfileResponse
import com.yybb.myapplication.data.network.dto.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
     fun getProfile(userId: String?): Flow<User> = flow {
        val token = sessionManager.authToken.first()
        if (token == null) {
            throw Exception("Not authenticated")
        }
        val response = if (userId == null) {
            apiService.getProfile()
        } else {
            apiService.getProfileByUsername(userId)
        }
        if (response.isSuccessful) {
            response.body()?.let {
                emit(it.toUser())
            } ?: throw Exception("Failed to get profile")
        } else {
            throw Exception("Failed to get profile: ${response.errorBody()?.string()}")
        }
    }

    suspend fun updateProfile(profession: String, bio: String?): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    return@withContext Result.failure(Exception("Not authenticated"))
                }
                val response = apiService.updateProfile(
                    UpdateProfileRequest(bio, profession)
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it.toUser())
                    } ?: Result.failure(Exception("Update failed"))
                } else {
                    Result.failure(Exception("Update failed: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun ProfileResponse.toUser(): User {
        return User(
            id = this.user.id.toString(),
            username = this.user.username,
            profession = this.profession,
            bio = this.bio ?: "",
            dateOfBirth = this.dateOfBirth,
            joinedDate = this.joinedDate,
            ownedSpaces = this.ownedSpaces.map {
                Space(
                    id = it.id.toString(),
                    name = it.name,
                    description = it.description
                )
            },
            joinedSpaces = this.joinedSpaces.map {
                Space(
                    id = it.id.toString(),
                    name = it.name,
                    description = it.description
                )
            }
        )
    }
}
