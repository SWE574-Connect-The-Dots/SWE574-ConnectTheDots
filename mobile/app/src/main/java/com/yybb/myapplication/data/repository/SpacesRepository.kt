package com.yybb.myapplication.data.repository

import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.CreateSpaceRequest
import com.yybb.myapplication.data.network.dto.CreateSpaceResponse
import com.yybb.myapplication.data.network.dto.SelectedTag
import com.yybb.myapplication.data.network.dto.TagDto
import com.yybb.myapplication.data.network.dto.TagRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpacesRepository @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    val isColorBlindTheme: Flow<Boolean> get() = userPreferencesRepository.isColorBlindTheme


    suspend fun getWikiTags(searchQuery: String): Result<List<TagDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }
                val response = apiService.getTagWikidata(searchQuery)
                if (response.isSuccessful) {
                    response.body()?.let { tagList ->
                        Result.success(tagList)
                    } ?: Result.failure(Exception("Failed to get tag from wikidata"))
                } else {
                    Result.failure(
                        Exception(
                            "Failed to get tag from wikidata: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Create or get existing tag
    private suspend fun createOrGetTag(tag: SelectedTag): Result<String> {
        return try {
            val request = TagRequest(
                name = tag.name,
                wikidataId = tag.wikidataId,
                wikidataLabel = tag.wikidataLabel
            )
            val response = apiService.createTag(request)

            when {
                response.isSuccessful && response.body() != null -> {
                    Result.success(response.body()!!.name)
                }
                response.code() == 409 -> {
                    // Tag already exists, use the tag name
                    Result.success(tag.name)
                }
                else -> {
                    Result.failure(Exception("Failed to create tag: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create space with tags
    suspend fun createSpace(
        title: String,
        description: String,
        selectedTags: List<SelectedTag>
    ): Result<CreateSpaceResponse> {
        return try {
            // Step 1: Create/get all tags
            val tagNames = mutableListOf<String>()
            for (tag in selectedTags) {
                val tagResult = createOrGetTag(tag)
                if (tagResult.isSuccess) {
                    tagNames.add(tagResult.getOrThrow())
                } else {
                    // If any tag fails, return the error
                    return Result.failure(tagResult.exceptionOrNull()
                        ?: Exception("Failed to create tag"))
                }
            }

            // Step 2: Create the space with tag names
            val createSpaceRequest = CreateSpaceRequest(
                title = title,
                description = description,
                tags = tagNames
            )

            val response = apiService.createSpace(createSpaceRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create space: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
