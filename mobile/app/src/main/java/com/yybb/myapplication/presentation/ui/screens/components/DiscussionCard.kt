package com.yybb.myapplication.presentation.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yybb.myapplication.data.model.Discussion
import com.yybb.myapplication.data.model.VoteType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiscussionCard(
    discussion: Discussion,
    onVoteClick: (String, VoteType) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Commenter name
            Text(
                text = discussion.username,
                fontSize = 14.sp,
                fontWeight = FontWeight.Companion.Bold,
                color = Color.Companion.Black,
                modifier = Modifier.Companion.padding(bottom = 4.dp)
            )

            // Comment text
            Text(
                text = discussion.text,
                fontSize = 13.sp,
                color = Color.Companion.Black,
                modifier = Modifier.Companion.padding(bottom = 8.dp)
            )

            // Bottom row with date and voting
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date
                Text(
                    text = discussion.getFormattedDate(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Companion.Start
                )

                // Voting section
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upvote button and count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onVoteClick(discussion.id.toString(), VoteType.UP) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Upvote",
                                tint = if (discussion.userReaction == "up") Color.Blue else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = discussion.upvotes.toString(),
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }

                    // Downvote button and count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onVoteClick(discussion.id.toString(), VoteType.DOWN) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Downvote",
                                tint = if (discussion.userReaction == "down") Color.Blue else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = discussion.downvotes.toString(),
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}