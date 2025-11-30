package com.yybb.myapplication.data.network.dto

import com.google.gson.annotations.SerializedName

data class ActivityStreamResponse(
    @SerializedName("@context")
    val context: String,
    val id: String,
    val type: String,
    val partOf: String?,
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("orderedItems")
    val orderedItems: List<ActivityItem>
)

data class ActivityItem(
    val id: String,
    val type: String,
    val actor: ActivityActor,
    val `object`: ActivityObject?,
    val target: ActivityTarget?,
    val summary: String,
    val published: String,
    val to: List<String>,
    val cc: List<String>,
    val payload: Map<String, Any?>?
)

data class ActivityActor(
    val type: String,
    val name: String
)

data class ActivityObject(
    val type: String,
    val id: String,
    val name: String
)

data class ActivityTarget(
    val type: String,
    val id: String,
    val name: String
)

