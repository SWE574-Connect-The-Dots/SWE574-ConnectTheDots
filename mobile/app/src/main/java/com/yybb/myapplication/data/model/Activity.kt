package com.yybb.myapplication.data.model

import java.time.ZonedDateTime

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
    val title: String,
    val description: String,
    val actorName: String,
    val timestamp: ZonedDateTime
)
