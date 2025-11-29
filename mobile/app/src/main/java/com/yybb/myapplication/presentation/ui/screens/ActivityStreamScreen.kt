package com.yybb.myapplication.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.R
import com.yybb.myapplication.data.model.Activity
import com.yybb.myapplication.data.model.ActivityType
import com.yybb.myapplication.presentation.ui.screens.components.ActivityCard
import com.yybb.myapplication.presentation.ui.viewmodel.ActivityStreamViewModel
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityStreamScreen(viewModel: ActivityStreamViewModel = hiltViewModel()) {
    val context = LocalContext.current
    
    // Mock data for activities
    val mockActivities = getMockActivities()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.activity_stream_title)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = mockActivities,
                key = { activity -> activity.id }
            ) { activity ->
                ActivityCard(
                    activity = activity,
                    onCardClick = {
                        Toast.makeText(
                            context,
                            context.getString(R.string.activity_card_clicked_toast),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onActorClick = { actorName ->
                        Toast.makeText(
                            context,
                            context.getString(R.string.actor_clicked_toast, actorName),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun getMockActivities(): List<Activity> {
    val now = ZonedDateTime.now()
    
    return listOf(
        Activity(
            id = "1",
            type = ActivityType.SPACE,
            title = "Space",
            description = "esranrzm created space 'test space for activity stream'",
            actorName = "esranrzm",
            timestamp = now.minusMinutes(45)
        ),
        Activity(
            id = "2",
            type = ActivityType.DISCUSSION,
            title = "Discussion",
            description = "dogaunal commented in 'test space for activity stream'",
            actorName = "dogaunal",
            timestamp = now.minusHours(1)
        ),
        Activity(
            id = "3",
            type = ActivityType.NODE,
            title = "Node",
            description = "dogaunal added node 'commentary'",
            actorName = "dogaunal",
            timestamp = now.minusHours(3)
        ),
        Activity(
            id = "4",
            type = ActivityType.REPORT,
            title = "Report",
            description = "esranrzm reported test space for activity stream",
            actorName = "esranrzm",
            timestamp = now.minusHours(4)
        ),
        Activity(
            id = "5",
            type = ActivityType.EDGE,
            title = "Edge",
            description = "esranrzm created edge 'commentary' - [notable work]-> 'Esra'",
            actorName = "esranrzm",
            timestamp = now.minusHours(5)
        ),
        Activity(
            id = "6",
            type = ActivityType.OTHER,
            title = "Other",
            description = "dogaunal commented in 'test space for activity stream'",
            actorName = "dogaunal",
            timestamp = now.minusHours(6)
        )
    )
}