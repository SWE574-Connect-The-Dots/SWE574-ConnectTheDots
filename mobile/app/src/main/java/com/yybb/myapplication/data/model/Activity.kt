package com.yybb.myapplication.data.model

import com.yybb.myapplication.data.network.dto.ActivityItem
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class ActivityType {
    SPACE,
    DISCUSSION,
    NODE,
    REPORT,
    EDGE,
    OTHER
}

data class Activity(
    val id: String,
    val type: ActivityType,
    val description: String,
    val actorName: String,
    val timestamp: ZonedDateTime,
    val objectType: String?,
    val objectId: String?,
    val objectName: String?,
    val targetId: String?,
    val targetType: String?,
    val payloadSpaceId: Int?,
    val payloadNodeId: Int?,
    val payloadEdgeId: Int?,
    val payloadDiscussionId: Int?,
    val payloadSourceId: Int?,
    val payloadTargetId: Int?
)

fun ActivityItem.toActivity(): Activity {
    val activityType = when {
        this.type == "Report" -> ActivityType.REPORT
        this.`object`?.type?.lowercase() == "node" -> ActivityType.NODE
        this.`object`?.type?.lowercase() == "edge" -> ActivityType.EDGE
        this.`object`?.type?.lowercase() == "space" -> ActivityType.SPACE
        this.`object`?.type?.lowercase() == "profile" -> ActivityType.REPORT
        this.`object`?.type?.lowercase() == "discussion" -> ActivityType.DISCUSSION
        else -> ActivityType.OTHER
    }

    val timestamp = try {
        ZonedDateTime.parse(this.published, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (e: Exception) {
        ZonedDateTime.now()
    }

    val payloadSpaceId = try {
        when (val spaceIdValue = this.payload?.get("space_id")) {
            is Number -> spaceIdValue.toInt()
            is String -> spaceIdValue.toIntOrNull()
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    val payloadNodeId = try {
        when (val nodeIdValue = this.payload?.get("node_id")) {
            is Number -> nodeIdValue.toInt()
            is String -> nodeIdValue.toIntOrNull()
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    val payloadEdgeId = try {
        when (val edgeIdValue = this.payload?.get("edge_id")) {
            is Number -> edgeIdValue.toInt()
            is String -> edgeIdValue.toIntOrNull()
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    val payloadDiscussionId = try {
        when (val discussionIdValue = this.payload?.get("discussion_id")) {
            is Number -> discussionIdValue.toInt()
            is String -> discussionIdValue.toIntOrNull()
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    val payloadSourceId = try {
        when (val sourceIdValue = this.payload?.get("source_id")) {
            is Number -> sourceIdValue.toInt()
            is String -> sourceIdValue.toIntOrNull()
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    val payloadTargetId = try {
        when (val targetIdValue = this.payload?.get("target_id")) {
            is Number -> targetIdValue.toInt()
            is String -> targetIdValue.toIntOrNull()
            else -> null
        }
    } catch (e: Exception) {
        null
    }
    
    return Activity(
        id = this.id,
        type = activityType,
        description = this.summary,
        actorName = this.actor.name,
        timestamp = timestamp,
        objectType = this.`object`?.type?.lowercase(),
        objectId = this.`object`?.id,
        objectName = this.`object`?.name,
        targetId = this.target?.id,
        targetType = this.target?.type,
        payloadSpaceId = payloadSpaceId,
        payloadNodeId = payloadNodeId,
        payloadEdgeId = payloadEdgeId,
        payloadDiscussionId = payloadDiscussionId,
        payloadSourceId = payloadSourceId,
        payloadTargetId = payloadTargetId
    )
}

