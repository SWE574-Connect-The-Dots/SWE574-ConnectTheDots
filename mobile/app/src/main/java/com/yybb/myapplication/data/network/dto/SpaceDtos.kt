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