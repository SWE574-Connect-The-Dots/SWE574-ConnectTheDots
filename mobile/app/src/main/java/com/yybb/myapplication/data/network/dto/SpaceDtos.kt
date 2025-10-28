package com.yybb.myapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class SpaceDto(
    val id: Int,
    @SerializedName("title")
    val name: String,
    val description: String
)

// Tag Creation Request
data class TagRequest(
    val name: String,
    @SerializedName("wikidata_id")
    val wikidataId: String,
    @SerializedName("wikidata_label")
    val wikidataLabel: String
)

// Tag Response
data class TagResponse(
    val id: Int,
    val name: String,
    @SerializedName("wikidata_id")
    val wikidataId: String?,
    @SerializedName("wikidata_label")
    val wikidataLabel: String?
)

// Create Space Request
data class CreateSpaceRequest(
    val title: String,
    val description: String,
    val tags: List<String> // List of tag names
)

// Create Space Response
data class CreateSpaceResponse(
    val id: Int,
    val title: String,
    val description: String,
    val tags: List<TagResponse>?,
    @SerializedName("created_at")
    val createdAt: String?
)

data class TagDto(
    val id: String,
    val label: String,
    val description: String,
    val url: String
)

// Space Details Response
data class SpaceDetailsResponse(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("creator_username")
    val creatorUsername: String,
    val tags: List<SpaceTagDto>,
    val collaborators: List<String>
)

data class SpaceTagDto(
    val id: Int,
    val name: String,
    @SerializedName("wikidata_id")
    val wikidataId: String,
    @SerializedName("wikidata_label")
    val wikidataLabel: String
)

// Discussion/Comment DTOs
data class DiscussionDto(
    val id: Int,
    val text: String,
    @SerializedName("created_at")
    val createdAt: String,
    val username: String,
    val upvotes: Int,
    val downvotes: Int,
    @SerializedName("user_reaction")
    val userReaction: String?
)

// Add Discussion Request
data class AddDiscussionRequest(
    val text: String
)

// Space Membership Response
data class SpaceMembershipResponse(
    val message: String,
    val success: Boolean
)