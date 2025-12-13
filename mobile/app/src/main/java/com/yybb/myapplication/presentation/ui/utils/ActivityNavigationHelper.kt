package com.yybb.myapplication.presentation.ui.utils

import androidx.navigation.NavHostController
import com.yybb.myapplication.data.model.Activity
import com.yybb.myapplication.presentation.navigation.Screen

fun navigateFromActivity(
    activity: Activity,
    navController: NavHostController
) {
    val objectType = activity.objectType?.lowercase()
    val spaceId = activity.payloadSpaceId 
        ?: activity.targetId?.toIntOrNull() 
        ?: activity.objectId?.toIntOrNull()
    
    when (objectType) {
        "node" -> {
            // Navigate to node details
            val nodeId = activity.objectId ?: activity.payloadNodeId?.toString() ?: return
            val nodeLabel = parseNodeLabel(activity.objectName, activity.description)
            val wikidataId = parseWikidataId(activity.objectName)
            
            if (spaceId != null) {
                navController.navigate(
                    Screen.SpaceNodeDetails.createRoute(
                        spaceId.toString(),
                        nodeId,
                        nodeLabel,
                        wikidataId
                    )
                )
            }
        }
        "edge" -> {
            // Navigate to edge details
            val edgeId = activity.objectId ?: activity.payloadEdgeId?.toString() ?: return
            val edgeLabel = parseEdgeLabel(activity.description)
            val sourceId = activity.payloadSourceId?.toString() ?: ""
            val targetId = activity.payloadTargetId?.toString() ?: ""
            val sourceName = parseNodeName(activity.description, isSource = true)
            val targetName = parseNodeName(activity.description, isSource = false)
            val currentNodeId = sourceId // Default to source, can be improved
            
            if (spaceId != null) {
                navController.navigate(
                    Screen.EdgeDetails.createRoute(
                        spaceId.toString(),
                        edgeId,
                        edgeLabel,
                        sourceId,
                        sourceName,
                        targetId,
                        targetName,
                        currentNodeId
                    )
                )
            }
        }
        "discussion" -> {
            // Stay on the same page - no navigation
        }
        "space" -> {
            // Navigate to space details
            val spaceIdToNavigate = activity.objectId?.toIntOrNull() 
                ?: activity.payloadSpaceId 
                ?: return
            navController.navigate(Screen.SpaceDetails.createRoute(spaceIdToNavigate))
        }
        "profile" -> {
            val reportedUsername = parseReportedUsername(activity.description)
            if (reportedUsername != null) {
                navController.navigate(Screen.Profile.createRoute(reportedUsername))
            }
        }
        else -> {
            // For other types, do nothing (stay on same page)
        }
    }
}

private fun parseNodeLabel(objectName: String?, description: String): String {
    if (objectName != null) {
        val match = Regex("'([^']+)'").find(description)
        if (match != null) {
            return match.groupValues[1]
        }
    }
    val match = Regex("'([^']+)'").find(description)
    return match?.groupValues?.get(1) ?: "Node"
}

private fun parseWikidataId(objectName: String?): String? {
    return null
}

private fun parseEdgeLabel(description: String): String {
    val match = Regex("\\[([^]]+)\\]").find(description)
    if (match != null) {
        return match.groupValues[1]
    }
    val edgeMatch = Regex("edge\\s+'([^']+)'").find(description)
    return edgeMatch?.groupValues?.get(1) ?: "Edge"
}

private fun parseNodeName(description: String, isSource: Boolean): String {
    val quotedMatches = Regex("'([^']+)'").findAll(description).toList()
    if (quotedMatches.size >= 2) {
        return if (isSource) {
            quotedMatches[0].groupValues[1]
        } else {
            quotedMatches[quotedMatches.size - 1].groupValues[1]
        }
    }
    return if (isSource) "Source" else "Target"
}

private fun parseReportedUsername(summary: String): String? {
    val parts = summary.split("reported")
    if (parts.size >= 2) {
        val reportedUsername = parts[1].trim()
        return if (reportedUsername.isNotEmpty()) {
            reportedUsername
        } else {
            null
        }
    }
    return null
}

