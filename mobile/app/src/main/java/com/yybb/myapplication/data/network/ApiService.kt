package com.yybb.myapplication.data.network

import com.yybb.myapplication.data.network.dto.AddDiscussionRequest
import com.yybb.myapplication.data.network.dto.CreateSpaceRequest
import com.yybb.myapplication.data.network.dto.CreateSpaceResponse
import com.yybb.myapplication.data.network.dto.DiscussionDto
import com.yybb.myapplication.data.network.dto.LoginRequest
import com.yybb.myapplication.data.network.dto.LoginResponse
import com.yybb.myapplication.data.network.dto.ProfileResponse
import com.yybb.myapplication.data.network.dto.RegisterRequest
import com.yybb.myapplication.data.network.dto.SpaceDetailsResponse
import com.yybb.myapplication.data.network.dto.SpaceMembershipResponse
import com.yybb.myapplication.data.network.dto.TagDto
import com.yybb.myapplication.data.network.dto.TagRequest
import com.yybb.myapplication.data.network.dto.TagResponse
import com.yybb.myapplication.data.network.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/register/")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("api/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/profiles/me/")
    suspend fun getProfile(): Response<ProfileResponse>
    
    @GET("api/profiles/{username}/user_profile/")
    suspend fun getProfileByUsername(@Path("username") username: String): Response<ProfileResponse>

    @PUT("api/profiles/update_profile/")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>

    @GET("api/tags/search_wikidata/")
    suspend fun getTagWikidata(@Query("query") tagQuery: String): Response<List<TagDto>>

    @POST("api/tags/")
    suspend fun createTag(@Body request: TagRequest): Response<TagResponse>

    @POST("api/spaces/")
    suspend fun createSpace(@Body request: CreateSpaceRequest): Response<CreateSpaceResponse>

    @GET("api/spaces/{id}/")
    suspend fun getSpaceById(@Path("id") id: String): Response<SpaceDetailsResponse>

    @GET("api/spaces/{id}/discussions/")
    suspend fun getSpaceDiscussions(@Path("id") id: String): Response<List<DiscussionDto>>

    @POST("api/spaces/{id}/discussions/add/")
    suspend fun addDiscussion(@Path("id") id: String, @Body request: AddDiscussionRequest): Response<DiscussionDto>

    @POST("api/spaces/{id}/leave/")
    suspend fun leaveSpace(@Path("id") id: String): Response<SpaceMembershipResponse>

    @POST("api/spaces/{id}/join/")
    suspend fun joinSpace(@Path("id") id: String): Response<SpaceMembershipResponse>

    @DELETE("api/spaces/{id}/")
    suspend fun deleteSpace(@Path("id") id: String): Response<Unit>
}
