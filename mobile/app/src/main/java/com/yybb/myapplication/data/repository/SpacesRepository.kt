package com.yybb.myapplication.data.repository

import android.content.Context
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.Discussion
import com.yybb.myapplication.data.model.SpaceDetails
import com.yybb.myapplication.data.model.toDiscussion
import com.yybb.myapplication.data.model.toSpaceDetails
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.AddDiscussionRequest
import com.yybb.myapplication.data.network.dto.CreateSpaceRequest
import com.yybb.myapplication.data.network.dto.CreateSpaceResponse
import com.yybb.myapplication.data.network.dto.DiscussionDto
import com.yybb.myapplication.data.network.dto.SpaceDetailsResponse
import com.yybb.myapplication.data.network.dto.SpaceMembershipResponse
import com.yybb.myapplication.data.network.dto.TagDto
import com.yybb.myapplication.data.network.dto.TagRequest
import com.yybb.myapplication.data.network.dto.VoteDiscussionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.yybb.myapplication.R
import dagger.hilt.android.qualifiers.ApplicationContext

class SpacesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    val isColorBlindTheme: Flow<Boolean> get() = userPreferencesRepository.isColorBlindTheme

    // Get trending spaces
    suspend fun getTrendingSpaces(): Result<List<SpaceDetailsResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }
                val response = apiService.getTrendingSpaces()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception(context.getString(R.string.failed_get_space_det_message)))
                } else {
                    Result.failure(Exception("${context.getString(R.string.failed_get_space_det_message)}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Get new spaces
    suspend fun getNewSpaces(): Result<List<SpaceDetailsResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }
                val response = apiService.getNewSpaces()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception(context.getString(R.string.failed_get_space_det_message)))
                } else {
                    Result.failure(Exception("${context.getString(R.string.failed_get_space_det_message)}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Get space details
    fun getSpaceDetails(spaceId: String): Flow<SpaceDetails> = flow {
        val token = sessionManager.authToken.first()
        if (token == null) {
            throw Exception("Not authenticated")
        }
        val response = apiService.getSpaceById(spaceId)
        if (response.isSuccessful) {
            response.body()?.let {
                emit(it.toSpaceDetails())
            } ?: throw Exception(context.getString(R.string.failed_get_space_det_message))
        } else {
            throw Exception("${context.getString(R.string.failed_get_space_det_message)}: ${response.errorBody()?.string()}")
        }
    }

    // Get space discussions
    fun getSpaceDiscussions(spaceId: String): Flow<List<Discussion>> = flow {
        val token = sessionManager.authToken.first()
        if (token == null) {
            throw Exception("Not authenticated")
        }
        val response = apiService.getSpaceDiscussions(spaceId)
        if (response.isSuccessful) {
            response.body()?.let { discussions ->
                emit(discussions.map { it.toDiscussion() })
            } ?: throw Exception(context.getString(R.string.failed_get_disc_message))
        } else {
            throw Exception("${context.getString(R.string.failed_get_disc_message)}: ${response.errorBody()?.string()}")
        }
    }

    // Add discussion to space
    suspend fun addDiscussion(spaceId: String, text: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }
                val request = AddDiscussionRequest(text = text)
                val response = apiService.addDiscussion(spaceId, request)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("${context.getString(R.string.failed_add_disc_message)}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Join space
    suspend fun joinSpace(spaceId: String): Result<SpaceMembershipResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }
                val response = apiService.joinSpace(spaceId)
                if (response.isSuccessful) {
                    response.body()?.let { 
                        Result.success(it)
                    } ?: Result.failure(Exception(context.getString(R.string.failed_join_space_message)))
                } else {
                    Result.failure(Exception("${context.getString(R.string.failed_join_space_message)}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Leave space
    suspend fun leaveSpace(spaceId: String): Result<SpaceMembershipResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }
                val response = apiService.leaveSpace(spaceId)
                if (response.isSuccessful) {
                    response.body()?.let { 
                        Result.success(it)
                    } ?: Result.failure(Exception(context.getString(R.string.failed_leave_space_message)))
                } else {
                    Result.failure(Exception("${context.getString(R.string.failed_leave_space_message)}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Delete space
    suspend fun deleteSpace(spaceId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }
                val response = apiService.deleteSpace(spaceId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("${context.getString(R.string.failed_delete_space_message)}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Vote on discussion
    suspend fun voteDiscussion(spaceId: String, discussionId: String, voteValue: String): Result<DiscussionDto> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }
                val request = VoteDiscussionRequest(value = voteValue)
                val response = apiService.voteDiscussion(spaceId, discussionId, request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it.discussion)
                    } ?: Result.failure(Exception(context.getString(R.string.failed_vote_discussion_message)))
                } else {
                    Result.failure(Exception("${context.getString(R.string.failed_vote_discussion_message)}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Search tag details from wikidata
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
    private suspend fun createOrGetTag(tag: TagDto): Result<String> {
        return try {
            val request = TagRequest(
                name = tag.label,
                wikidataId = tag.id,
                wikidataLabel = tag.label
            )
            val response = apiService.createTag(request)

            when {
                response.isSuccessful && response.body() != null -> {
                    Result.success(response.body()!!.wikidataLabel ?: response.body()!!.name)
                }
                response.code() == 409 -> {
                    Result.success(tag.label)
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
        selectedTags: List<TagDto>
    ): Result<CreateSpaceResponse> {
        return try {
            val tagNames = mutableListOf<String>()
            for (tag in selectedTags) {
                val tagResult = createOrGetTag(tag)
                if (tagResult.isSuccess) {
                    tagNames.add(tagResult.getOrThrow())
                } else {
                    return Result.failure(tagResult.exceptionOrNull()
                        ?: Exception(context.getString(R.string.failed_create_tag_message)))
                }
            }
            val createSpaceRequest = CreateSpaceRequest(
                title = title,
                description = description,
                tags = tagNames
            )
            val response = apiService.createSpace(createSpaceRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("${context.getString(R.string.failed_create_space_message)}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
