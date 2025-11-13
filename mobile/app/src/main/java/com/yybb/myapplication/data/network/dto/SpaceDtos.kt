package com.yybb.myapplication.data.network.dto

import com.google.gson.JsonElement
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

data class SpaceNodeResponse(
    val id: Int,
    val label: String,
    @SerializedName("wikidata_id")
    val wikidataId: String?,
    val country: String?,
    val city: String?,
    val district: String?,
    val street: String?,
    val latitude: String?,
    val longitude: String?,
    @SerializedName("location_name")
    val locationName: String?
)

data class SpaceEdgeResponse(
    val id: Int,
    val source: Int,
    val target: Int,
    val label: String,
    @SerializedName("wikidata_property_id")
    val wikidataPropertyId: String?
)

data class AddEdgeRequest(
    @SerializedName("source_id")
    val sourceId: String,
    @SerializedName("target_id")
    val targetId: String,
    val label: String,
    @SerializedName("wikidata_property_id")
    val wikidataPropertyId: String
)

data class AddEdgeResponse(
    val message: String,
    @SerializedName("edge_id")
    val edgeId: Int
)

data class CreateSnapshotResponse(
    @SerializedName("snapshot_id")
    val snapshotId: Int,
    @SerializedName("created_at")
    val createdAt: String
)

data class NodePropertyResponse(
    @SerializedName("statement_id")
    val statementId: String,
    @SerializedName("property_id")
    val propertyId: String,
    @SerializedName("property_label")
    val propertyLabel: String,
    @SerializedName("property_value")
    val propertyValue: JsonElement?,
    val display: String?
)

data class UpdateNodePropertyRequest(
    @SerializedName("statement_id")
    val statementId: String,
    @SerializedName("property")
    val propertyId: String,
    @SerializedName("property_label")
    val propertyLabel: String,
    @SerializedName("value")
    val propertyValue: JsonElement?,
    val display: String?
)

data class NodeWikidataPropertyResponse(
    @SerializedName("statement_id")
    val statementId: String,
    @SerializedName("property")
    val propertyId: String,
    @SerializedName("property_label")
    val propertyLabel: String,
    @SerializedName("value")
    val propertyValue: JsonElement?,
    val display: String?
)

data class UpdateNodePropertiesRequest(
    @SerializedName("selected_properties")
    val selectedProperties: List<UpdateNodePropertyItem>
)

data class UpdateNodePropertyItem(
    @SerializedName("statement_id")
    val statementId: String,
    val property: String,
    @SerializedName("property_label")
    val propertyLabel: String,
    val value: JsonElement?
)

data class UpdateNodePropertiesResponse(
    val message: String
)

data class DeleteNodeResponse(
    val message: String
)

// Vote Discussion Request
data class VoteDiscussionRequest(
    val value: String // "up" or "down"
)

// Vote Discussion Response
data class VoteDiscussionResponse(
    @SerializedName("toggled_off")
    val toggledOff: Boolean,
    val discussion: DiscussionDto
)