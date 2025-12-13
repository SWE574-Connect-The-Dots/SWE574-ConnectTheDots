package com.yybb.myapplication.data.repository

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.ReportReasonItem
import com.yybb.myapplication.data.network.dto.ReportReasonsData
import com.yybb.myapplication.data.network.dto.ReportResponse
import com.yybb.myapplication.data.network.dto.SubmitReportRequest
import com.yybb.myapplication.data.network.dto.SubmitReportResponse
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class SpacesRepositoryReportTest {

    private lateinit var repository: SpacesRepository
    private lateinit var mockContext: Context
    private lateinit var mockApiService: ApiService
    private lateinit var mockSessionManager: SessionManager
    private lateinit var mockUserPreferencesRepository: com.yybb.myapplication.data.UserPreferencesRepository

    private val token = "test_token"

    @Before
    fun setUp() {
        mockContext = mock()
        mockApiService = mock()
        mockSessionManager = mock()
        mockUserPreferencesRepository = mock()
        repository = SpacesRepository(
            mockContext,
            mockUserPreferencesRepository,
            mockApiService,
            mockSessionManager
        )

        whenever(mockSessionManager.authToken).thenReturn(flowOf(token))
    }

    @Test
    fun `getReportReasons should return space reasons on success`() = runTest {
        val spaceReasons = listOf(
            ReportReasonItem("INAPPROPRIATE", "Inappropriate content"),
            ReportReasonItem("SPAM", "Spam")
        )
        val reportResponse = ReportResponse(
            version = 1,
            reasons = ReportReasonsData(
                space = spaceReasons,
                node = emptyList(),
                discussion = emptyList(),
                profile = emptyList()
            )
        )
        val response = Response.success(reportResponse)
        whenever(mockApiService.getReportReasons()).thenReturn(response)

        val result = repository.getReportReasons("space")

        assertTrue(result.isSuccess)
        val reasons = result.getOrNull()!!
        assertEquals(2, reasons.size)
        assertEquals("INAPPROPRIATE", reasons[0].code)
        assertEquals("Inappropriate content", reasons[0].label)
        assertEquals("SPAM", reasons[1].code)
        assertEquals("Spam", reasons[1].label)
        verify(mockApiService).getReportReasons()
    }

    @Test
    fun `getReportReasons should return node reasons on success`() = runTest {
        val nodeReasons = listOf(
            ReportReasonItem("INAPPROPRIATE", "Inappropriate content"),
            ReportReasonItem("DUPLICATE_NODE", "Duplicate node")
        )
        val reportResponse = ReportResponse(
            version = 1,
            reasons = ReportReasonsData(
                space = emptyList(),
                node = nodeReasons,
                discussion = emptyList(),
                profile = emptyList()
            )
        )
        val response = Response.success(reportResponse)
        whenever(mockApiService.getReportReasons()).thenReturn(response)

        val result = repository.getReportReasons("node")

        assertTrue(result.isSuccess)
        val reasons = result.getOrNull()!!
        assertEquals(2, reasons.size)
        assertEquals("INAPPROPRIATE", reasons[0].code)
        assertEquals("DUPLICATE_NODE", reasons[1].code)
    }

    @Test
    fun `getReportReasons should return discussion reasons on success`() = runTest {
        val discussionReasons = listOf(
            ReportReasonItem("INAPPROPRIATE", "Inappropriate content"),
            ReportReasonItem("OFF_TOPIC", "Off-topic")
        )
        val reportResponse = ReportResponse(
            version = 1,
            reasons = ReportReasonsData(
                space = emptyList(),
                node = emptyList(),
                discussion = discussionReasons,
                profile = emptyList()
            )
        )
        val response = Response.success(reportResponse)
        whenever(mockApiService.getReportReasons()).thenReturn(response)

        val result = repository.getReportReasons("discussion")

        assertTrue(result.isSuccess)
        val reasons = result.getOrNull()!!
        assertEquals(2, reasons.size)
        assertEquals("INAPPROPRIATE", reasons[0].code)
        assertEquals("OFF_TOPIC", reasons[1].code)
    }

    @Test
    fun `getReportReasons should return profile reasons on success`() = runTest {
        val profileReasons = listOf(
            ReportReasonItem("INAPPROPRIATE", "Inappropriate content"),
            ReportReasonItem("FAKE_ACCOUNT", "Fake account")
        )
        val reportResponse = ReportResponse(
            version = 1,
            reasons = ReportReasonsData(
                space = emptyList(),
                node = emptyList(),
                discussion = emptyList(),
                profile = profileReasons
            )
        )
        val response = Response.success(reportResponse)
        whenever(mockApiService.getReportReasons()).thenReturn(response)

        val result = repository.getReportReasons("profile")

        assertTrue(result.isSuccess)
        val reasons = result.getOrNull()!!
        assertEquals(2, reasons.size)
        assertEquals("INAPPROPRIATE", reasons[0].code)
        assertEquals("FAKE_ACCOUNT", reasons[1].code)
    }

    @Test
    fun `getReportReasons should return empty list for unknown content type`() = runTest {
        val reportResponse = ReportResponse(
            version = 1,
            reasons = ReportReasonsData(
                space = emptyList(),
                node = emptyList(),
                discussion = emptyList(),
                profile = emptyList()
            )
        )
        val response = Response.success(reportResponse)
        whenever(mockApiService.getReportReasons()).thenReturn(response)

        val result = repository.getReportReasons("unknown")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getReportReasons should return error on API failure`() = runTest {
        val response = Response.error<ReportResponse>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.getReportReasons()).thenReturn(response)

        val result = repository.getReportReasons("space")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Failed to get report reasons"))
    }

    @Test
    fun `getReportReasons should return error when not authenticated`() = runTest {
        whenever(mockSessionManager.authToken).thenReturn(flowOf(null))

        val result = repository.getReportReasons("space")

        assertTrue(result.isFailure)
        assertEquals("Not authenticated", result.exceptionOrNull()!!.message)
    }

    @Test
    fun `submitReport should submit report successfully`() = runTest {
        val submitResponse = SubmitReportResponse(
            id = 1,
            contentType = "space",
            contentId = 123,
            reason = "INAPPROPRIATE",
            status = "OPEN",
            space = 123,
            reporter = 1,
            reporterUsername = "testuser",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            entityReportCount = 1,
            entityIsReported = true
        )
        val response = Response.success(submitResponse)
        whenever(mockApiService.submitReport(any())).thenReturn(response)

        val result = repository.submitReport("space", 123, "INAPPROPRIATE")

        assertTrue(result.isSuccess)
        val report = result.getOrNull()!!
        assertEquals(1, report.id)
        assertEquals("space", report.contentType)
        assertEquals(123, report.contentId)
        assertEquals("INAPPROPRIATE", report.reason)
        verify(mockApiService).submitReport(
            SubmitReportRequest("space", 123, "INAPPROPRIATE")
        )
    }

    @Test
    fun `submitReport should return error on API failure`() = runTest {
        val response = Response.error<SubmitReportResponse>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.submitReport(any())).thenReturn(response)

        val result = repository.submitReport("space", 12, "INAPPROPRIATE")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Failed to submit report"))
    }

    @Test
    fun `submitReport should return error when not authenticated`() = runTest {
        whenever(mockSessionManager.authToken).thenReturn(flowOf(null))

        val result = repository.submitReport("space", 1, "INAPPROPRIATE")

        assertTrue(result.isFailure)
        assertEquals("Not authenticated", result.exceptionOrNull()!!.message)
    }
}

