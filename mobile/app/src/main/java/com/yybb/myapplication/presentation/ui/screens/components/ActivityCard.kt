package com.yybb.myapplication.presentation.ui.screens.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yybb.myapplication.R
import com.yybb.myapplication.data.model.Activity
import com.yybb.myapplication.data.model.ActivityType

@Composable
fun ActivityCard(
    activity: Activity,
    onCardClick: () -> Unit,
    onActorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    useSmallerFont: Boolean = false,
    isClickable: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isClickable) {
                    Modifier.clickable { onCardClick() }
                } else {
                    Modifier
                }
            )
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getActivityIconRes(activity.type, activity.objectType)),
                contentDescription = activity.type.name,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = activity.description,
                    style = if (useSmallerFont) {
                        TextStyle(fontSize = 12.sp)
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    color = Color.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.actorName,
                        style = if (useSmallerFont) {
                            TextStyle(fontSize = 11.sp)
                        } else {
                            MaterialTheme.typography.bodySmall
                        },
                        color = Color(0xFF4FC3F7), // Light blue
                        modifier = Modifier.clickable { onActorClick(activity.actorName) }
                    )

                    Text(
                        text = formatRelativeTime(activity.timestamp),
                        style = if (useSmallerFont) {
                            TextStyle(fontSize = 11.sp)
                        } else {
                            MaterialTheme.typography.bodySmall
                        },
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun getActivityIconRes(type: ActivityType, objectType: String? = null): Int {
    return when (type) {
        ActivityType.REPORT -> R.drawable.report
        ActivityType.NODE -> R.drawable.node
        ActivityType.EDGE -> R.drawable.edge
        ActivityType.SPACE -> R.drawable.space
        ActivityType.DISCUSSION -> R.drawable.comment
        ActivityType.OTHER -> {
            when (objectType?.lowercase()) {
                "profile" -> R.drawable.report
                else -> R.drawable.other
            }
        }
    }
}

private fun formatRelativeTime(timestamp: java.time.ZonedDateTime): String {
    val now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
    val duration = java.time.Duration.between(timestamp, now)
    
    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()
    
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
        days >= 1 -> "$days day${if (days != 1L) "s" else ""} ago"
        else -> "just now"
    }
}
