package com.yybb.myapplication.data.repository

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.SpaceNodeResponse
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class SpaceNodesRepositoryTest {

    private lateinit var repository: SpaceNodesRepository
    private lateinit var mockContext: Context
    private lateinit var mockApiService: ApiService
    private lateinit var mockSessionManager: SessionManager

    private val spaceId = "space123"
    private val token = "test_token"

    @Before
    fun setUp() {
        mockContext = mock()
        mockApiService = mock()
        mockSessionManager = mock()
        repository = SpaceNodesRepository(mockContext, mockApiService, mockSessionManager)

        whenever(mockSessionManager.authToken).thenReturn(flowOf(token))
        whenever(mockContext.getString(R.string.space_nodes_error_message))
            .thenReturn("Error loading nodes")
    }

    @Test
    fun `getSpaceNodes should return nodes on success`() = runTest {
        val mockResponse = listOf(
            SpaceNodeResponse(1, "Node 1", null, null, null, null, null, null, null, null, null),
            SpaceNodeResponse(2, "Node 2", null, null, null, null, null, null, null, null, null)
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.getSpaceNodes(spaceId)).thenReturn(response)

        val result = repository.getSpaceNodes(spaceId)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertEquals("Node 1", result.getOrNull()!![0].label)
        assertEquals("Node 2", result.getOrNull()!![1].label)
        verify(mockApiService).getSpaceNodes(spaceId)
    }

    @Test
    fun `getSpaceNodes should return error on failure`() = runTest {
        val response = Response.error<List<SpaceNodeResponse>>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.getSpaceNodes(spaceId)).thenReturn(response)

        val result = repository.getSpaceNodes(spaceId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error loading nodes"))
    }

    @Test
    fun `getSpaceNodes should return error when not authenticated`() = runTest {
        whenever(mockSessionManager.authToken).thenReturn(flowOf(null))

        val result = repository.getSpaceNodes(spaceId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Not authenticated"))
    }

    @Test
    fun `getSpaceNodes should return error on null response body`() = runTest {
        val response = Response.success<List<SpaceNodeResponse>>(null)
        whenever(mockApiService.getSpaceNodes(spaceId)).thenReturn(response)

        val result = repository.getSpaceNodes(spaceId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error loading nodes"))
    }

    @Test
    fun `getSpaceNodes should handle exception`() = runTest {
        val exception = RuntimeException("Network error")
        whenever(mockApiService.getSpaceNodes(spaceId)).thenThrow(exception)

        val result = repository.getSpaceNodes(spaceId)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()!!.message)
    }

    @Test
    fun `getSpaceNodes should return nodes with all fields populated`() = runTest {
        val mockResponse = listOf(
            SpaceNodeResponse(
                id = 1,
                label = "Test Node",
                wikidataId = "Q123",
                country = "Country",
                city = "City",
                district = "District",
                street = "Street",
                latitude = "40.7128",
                longitude = "-74.0060",
                locationName = "Location Name",
                createdAt = null
            )
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.getSpaceNodes(spaceId)).thenReturn(response)

        val result = repository.getSpaceNodes(spaceId)

        assertTrue(result.isSuccess)
        val node = result.getOrNull()!![0]
        assertEquals(1, node.id)
        assertEquals("Test Node", node.label)
        assertEquals("Q123", node.wikidataId)
        assertEquals("Country", node.country)
        assertEquals("City", node.city)
        assertEquals("District", node.district)
        assertEquals("Street", node.street)
        assertEquals("40.7128", node.latitude)
        assertEquals("-74.0060", node.longitude)
        assertEquals("Location Name", node.locationName)
    }
}

