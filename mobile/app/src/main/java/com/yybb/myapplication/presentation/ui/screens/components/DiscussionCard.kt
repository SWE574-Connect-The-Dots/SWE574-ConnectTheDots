package com.yybb.myapplication.presentation.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yybb.myapplication.presentation.ui.screens.Comment

@Composable
fun DiscussionCard(
    comment: Comment,
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
                text = comment.commenterName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Companion.Bold,
                color = Color.Companion.Black,
                modifier = Modifier.Companion.padding(bottom = 4.dp)
            )

            // Comment text
            Text(
                text = comment.comment,
                fontSize = 13.sp,
                color = Color.Companion.Black,
                modifier = Modifier.Companion.padding(bottom = 8.dp)
            )

            // Date (right aligned)
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = comment.getFormattedDate(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Companion.End
                )
            }
        }
    }
}