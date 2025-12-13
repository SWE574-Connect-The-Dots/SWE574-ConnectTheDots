package com.yybb.myapplication.presentation.ui.viewmodel

import app.cash.turbine.test
import com.yybb.myapplication.data.UserPreferencesRepository
import com.yybb.myapplication.data.model.Activity
import com.yybb.myapplication.data.model.ActivityType
import com.yybb.myapplication.data.repository.ActivityStreamRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityStreamViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ActivityStreamViewModel
    private lateinit var mockRepository: ActivityStreamRepository
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository

    private val currentUsername = "testuser"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        mockUserPreferencesRepository = mock()
        
        whenever(mockUserPreferencesRepository.getCurrentUsernameSync()).thenReturn(currentUsername)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestActivity(
        id: String = "1",
        type: ActivityType = ActivityType.SPACE,
        description: String = "Test activity",
        actorName: String = "actor1",
        timestamp: ZonedDateTime = ZonedDateTime.now()
    ): Activity {
        return Activity(
            id = id,
            type = type,
            description = description,
            actorName = actorName,
            timestamp = timestamp,
            objectType = "space",
            objectId = "1",
            objectName = "Space:1",
            targetId = null,
            targetType = null,
            payloadSpaceId = 1,
            payloadNodeId = null,
            payloadEdgeId = null,
            payloadDiscussionId = null,
            payloadSourceId = null,
            payloadTargetId = null
        )
    }

    @Test
    fun `init should load activities on creation`() = runTest {
        val activities = listOf(
            createTestActivity(id = "1"),
            createTestActivity(id = "2", actorName = "actor2")
        )

        whenever(mockRepository.getActivityStream(eq(100), any())).thenReturn(
            kotlin.Result.success(activities)
        )

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        
        advanceUntilIdle()

        viewModel.activities.test {
            val emitted = awaitItem()
            assertEquals(2, emitted.size)
            assertEquals("1", emitted[0].id)
            assertEquals("2", emitted[1].id)
            cancelAndIgnoreRemainingEvents()
        }

        verify(mockRepository, times(1)).getActivityStream(eq(100), any())
    }

    @Test
    fun `loadActivities should update activities on success`() = runTest {
        val activities = listOf(
            createTestActivity(id = "1"),
            createTestActivity(id = "2"),
            createTestActivity(id = "3")
        )

        whenever(mockRepository.getActivityStream(eq(100), any())).thenReturn(
            kotlin.Result.success(activities)
        )

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        
        advanceUntilIdle()

        viewModel.activities.test {
            val emitted = awaitItem()
            assertEquals(3, emitted.size)
            assertEquals("1", emitted[0].id)
            assertEquals("2", emitted[1].id)
            assertEquals("3", emitted[2].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadActivities should set error on failure`() = runTest {
        val errorMessage = "Network error"
        
        whenever(mockRepository.getActivityStream(eq(100), any())).thenReturn(
            kotlin.Result.failure(Exception(errorMessage))
        )

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            assertEquals(errorMessage, error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.activities.test {
            val activities = awaitItem()
            assertTrue(activities.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadActivities should clear previous error`() = runTest {
        val activities = listOf(createTestActivity())

        whenever(mockRepository.getActivityStream(eq(100), any()))
            .thenReturn(kotlin.Result.failure(Exception("First error")))
            .thenReturn(kotlin.Result.success(activities))

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.error.test {
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.loadActivities()
        advanceUntilIdle()

        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.activities.test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh should reload activities`() = runTest {
        val initialActivities = listOf(createTestActivity(id = "1"))
        val refreshedActivities = listOf(
            createTestActivity(id = "1"),
            createTestActivity(id = "2")
        )

        whenever(mockRepository.getActivityStream(eq(100), any()))
            .thenReturn(kotlin.Result.success(initialActivities))
            .thenReturn(kotlin.Result.success(refreshedActivities))

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.activities.test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.refresh()
        advanceUntilIdle()

        viewModel.activities.test {
            val emitted = awaitItem()
            assertEquals(2, emitted.size)
            cancelAndIgnoreRemainingEvents()
        }

        verify(mockRepository, times(2)).getActivityStream(eq(100), any())
    }

    @Test
    fun `loadActivities should use custom limit parameter`() = runTest {
        val activities = listOf(createTestActivity())

        whenever(mockRepository.getActivityStream(eq(100), any())).thenReturn(
            kotlin.Result.success(activities)
        )
        whenever(mockRepository.getActivityStream(eq(50), any())).thenReturn(
            kotlin.Result.success(activities)
        )

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.loadActivities(limit = 50)
        advanceUntilIdle()

        verify(mockRepository, times(1)).getActivityStream(eq(100), any()) // init call
        verify(mockRepository, times(1)).getActivityStream(eq(50), any()) // custom limit call
    }

    @Test
    fun `loadActivities should use custom since parameter`() = runTest {
        val activities = listOf(createTestActivity())
        val customSince = "2025-01-01T00:00:00Z"

        whenever(mockRepository.getActivityStream(eq(100), eq(customSince))).thenReturn(
            kotlin.Result.success(activities)
        )

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.loadActivities(since = customSince)
        advanceUntilIdle()

        verify(mockRepository).getActivityStream(eq(100), eq(customSince))
    }

    @Test
    fun `getCurrentUsername should return username from repository`() = runTest {
        whenever(mockRepository.getActivityStream(eq(100), any())).thenReturn(
            kotlin.Result.success(emptyList())
        )

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        val username = viewModel.getCurrentUsername()
        assertEquals(currentUsername, username)
        
        verify(mockUserPreferencesRepository).getCurrentUsernameSync()
    }

    @Test
    fun `loadActivities with empty result should set empty list`() = runTest {
        whenever(mockRepository.getActivityStream(eq(100), any())).thenReturn(
            kotlin.Result.success(emptyList())
        )

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.activities.test {
            val activities = awaitItem()
            assertTrue(activities.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple refresh calls should reload multiple times`() = runTest {
        val activities = listOf(createTestActivity())

        whenever(mockRepository.getActivityStream(eq(100), any())).thenReturn(
            kotlin.Result.success(activities)
        )

        viewModel = ActivityStreamViewModel(mockRepository, mockUserPreferencesRepository)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()
        
        viewModel.refresh()
        advanceUntilIdle()
        
        viewModel.refresh()
        advanceUntilIdle()

        verify(mockRepository, times(4)).getActivityStream(eq(100), any()) // init + 3 refreshes
    }
}

