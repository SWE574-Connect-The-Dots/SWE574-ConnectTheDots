package com.yybb.myapplication.data.model

import com.yybb.myapplication.data.network.dto.SpaceDetailsResponse
import com.yybb.myapplication.data.network.dto.SpaceTagDto
import com.yybb.myapplication.data.network.dto.DiscussionDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SpaceDetails(
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: String,
    val creatorUsername: String,
    val tags: List<SpaceTag>,
    val collaborators: List<String>,
    val isArchived: Boolean = false,
    val discussions: List<Discussion> = emptyList()
)

data class SpaceTag(
    val id: Int,
    val name: String,
    val wikidataId: String,
    val wikidataLabel: String
)

data class Discussion(
    val id: Int,
    val text: String,
    val createdAt: String,
    val username: String,
    val upvotes: Int,
    val downvotes: Int,
    val userReaction: String?
) {
    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(createdAt)
            "Sent: ${outputFormat.format(date ?: Date())}"
        } catch (e: Exception) {
            "Sent: $createdAt"
        }
    }
}

enum class VoteType {
    NONE, UP, DOWN
}

fun SpaceDetailsResponse.toSpaceDetails(): SpaceDetails {
    return SpaceDetails(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        creatorUsername = creatorUsername,
        tags = tags.map { it.toSpaceTag() },
        collaborators = collaborators,
        isArchived = isArchived
    )
}

fun SpaceTagDto.toSpaceTag(): SpaceTag {
    return SpaceTag(
        id = id,
        name = name,
        wikidataId = wikidataId,
        wikidataLabel = wikidataLabel
    )
}

fun DiscussionDto.toDiscussion(): Discussion {
    return Discussion(
        id = id,
        text = text,
        createdAt = createdAt,
        username = username,
        upvotes = upvotes,
        downvotes = downvotes,
        userReaction = userReaction
    )
}
