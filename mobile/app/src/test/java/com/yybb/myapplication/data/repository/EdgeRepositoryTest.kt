package com.yybb.myapplication.data.repository

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.AddEdgeResponse
import com.yybb.myapplication.data.network.dto.DeleteEdgeResponse
import com.yybb.myapplication.data.network.dto.SpaceEdgeResponse
import com.yybb.myapplication.data.network.dto.UpdateEdgeResponse
import com.yybb.myapplication.data.network.dto.WikidataPropertyDto
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class EdgeRepositoryTest {

    private lateinit var repository: SpaceNodeDetailsRepository
    private lateinit var mockContext: Context
    private lateinit var mockApiService: ApiService
    private lateinit var mockSessionManager: SessionManager

    private val spaceId = "space123"
    private val edgeId = "edge456"
    private val token = "test_token"

    @Before
    fun setUp() {
        mockContext = mock()
        mockApiService = mock()
        mockSessionManager = mock()
        repository = SpaceNodeDetailsRepository(mockContext, mockApiService, mockSessionManager)

        whenever(mockSessionManager.authToken).thenReturn(flowOf(token))
        whenever(mockContext.getString(R.string.space_node_connections_error))
            .thenReturn("Error loading connections")
        whenever(mockContext.getString(R.string.add_edge_property_search_error))
            .thenReturn("Error searching properties")
        whenever(mockContext.getString(R.string.add_edge_service_error))
            .thenReturn("Error adding edge")
        whenever(mockContext.getString(R.string.update_edge_service_error))
            .thenReturn("Error updating edge")
        whenever(mockContext.getString(R.string.delete_edge_service_error))
            .thenReturn("Error deleting edge")
    }

    @Test
    fun `getSpaceEdges should return edges on success`() = runTest {
        // Given
        val mockResponse = listOf(
            SpaceEdgeResponse(1, 1, 2, "Edge 1", "P1"),
            SpaceEdgeResponse(2, 2, 3, "Edge 2", "P2")
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.getSpaceEdges(spaceId)).thenReturn(response)

        // When
        val result = repository.getSpaceEdges(spaceId)

        // Then
        assertTrue(result.isSuccess)
        val edges = result.getOrNull()!!
        assertEquals(2, edges.size)
        assertEquals(1, edges[0].id)
        assertEquals(1, edges[0].source)
        assertEquals(2, edges[0].target)
        assertEquals("Edge 1", edges[0].label)
        assertEquals("P1", edges[0].wikidataPropertyId)
        verify(mockApiService).getSpaceEdges(spaceId)
    }

    @Test
    fun `getSpaceEdges should return error on failure`() = runTest {
        // Given
        val response = Response.error<List<SpaceEdgeResponse>>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.getSpaceEdges(spaceId)).thenReturn(response)

        // When
        val result = repository.getSpaceEdges(spaceId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error loading connections"))
    }

    @Test
    fun `getSpaceEdges should return error when not authenticated`() = runTest {
        // Given
        whenever(mockSessionManager.authToken).thenReturn(flowOf(null))

        // When
        val result = repository.getSpaceEdges(spaceId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Not authenticated"))
    }

    @Test
    fun `getSpaceEdges should return error on null response body`() = runTest {
        // Given
        val response = Response.success<List<SpaceEdgeResponse>>(null)
        whenever(mockApiService.getSpaceEdges(spaceId)).thenReturn(response)

        // When
        val result = repository.getSpaceEdges(spaceId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error loading connections"))
    }

    @Test
    fun `addEdgeToSpaceGraph should return edge response on success`() = runTest {
        // Given
        val sourceId = "source123"
        val targetId = "target456"
        val label = "New Edge"
        val wikidataPropertyId = "P1"
        val mockResponse = AddEdgeResponse("Edge added successfully", 1)
        val response = Response.success(mockResponse)
        whenever(mockApiService.addEdgeToSpaceGraph(eq(spaceId), any())).thenReturn(response)

        // When
        val result = repository.addEdgeToSpaceGraph(spaceId, sourceId, targetId, label, wikidataPropertyId)

        // Then
        assertTrue(result.isSuccess)
        val edgeResponse = result.getOrNull()!!
        assertEquals(1, edgeResponse.edgeId)
        assertEquals("Edge added successfully", edgeResponse.message)
        verify(mockApiService).addEdgeToSpaceGraph(eq(spaceId), any())
    }

    @Test
    fun `addEdgeToSpaceGraph should create correct request`() = runTest {
        // Given
        val sourceId = "source123"
        val targetId = "target456"
        val label = "New Edge"
        val wikidataPropertyId = "P1"
        val mockResponse = AddEdgeResponse("Edge added", 1)
        val response = Response.success(mockResponse)
        whenever(mockApiService.addEdgeToSpaceGraph(eq(spaceId), any())).thenReturn(response)

        // When
        val result = repository.addEdgeToSpaceGraph(spaceId, sourceId, targetId, label, wikidataPropertyId)

        // Then
        assertTrue(result.isSuccess)
        verify(mockApiService).addEdgeToSpaceGraph(eq(spaceId), any())
    }

    @Test
    fun `addEdgeToSpaceGraph should return error on failure`() = runTest {
        // Given
        val response = Response.error<AddEdgeResponse>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.addEdgeToSpaceGraph(eq(spaceId), any())).thenReturn(response)

        // When
        val result = repository.addEdgeToSpaceGraph(spaceId, "source1", "target1", "label", "")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error adding edge"))
    }

    @Test
    fun `addEdgeToSpaceGraph should return error on null response body`() = runTest {
        // Given
        val response = Response.success<AddEdgeResponse>(null)
        whenever(mockApiService.addEdgeToSpaceGraph(eq(spaceId), any())).thenReturn(response)

        // When
        val result = repository.addEdgeToSpaceGraph(spaceId, "source1", "target1", "label", "")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error adding edge"))
    }

    @Test
    fun `updateEdgeDetails should return success on update`() = runTest {
        // Given
        val label = "Updated Edge Label"
        val sourceId = "source123"
        val targetId = "target456"
        val wikidataPropertyId = "P1"
        val mockResponse = UpdateEdgeResponse("Edge updated successfully")
        val response = Response.success(mockResponse)
        whenever(mockApiService.updateEdgeDetails(eq(spaceId), eq(edgeId), any())).thenReturn(response)

        // When
        val result = repository.updateEdgeDetails(spaceId, edgeId, label, sourceId, targetId, wikidataPropertyId)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        verify(mockApiService).updateEdgeDetails(eq(spaceId), eq(edgeId), any())
    }

    @Test
    fun `updateEdgeDetails should create correct request`() = runTest {
        // Given
        val label = "Updated Edge Label"
        val sourceId = "source123"
        val targetId = "target456"
        val wikidataPropertyId = "P1"
        val mockResponse = UpdateEdgeResponse("Edge updated")
        val response = Response.success(mockResponse)
        whenever(mockApiService.updateEdgeDetails(eq(spaceId), eq(edgeId), any())).thenReturn(response)

        // When
        val result = repository.updateEdgeDetails(spaceId, edgeId, label, sourceId, targetId, wikidataPropertyId)

        // Then
        assertTrue(result.isSuccess)
        verify(mockApiService).updateEdgeDetails(eq(spaceId), eq(edgeId), any())
    }

    @Test
    fun `updateEdgeDetails should return error on failure`() = runTest {
        // Given
        val response = Response.error<UpdateEdgeResponse>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.updateEdgeDetails(eq(spaceId), eq(edgeId), any())).thenReturn(response)

        // When
        val result = repository.updateEdgeDetails(spaceId, edgeId, "label", "source1", "target1", "")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error updating edge"))
    }

    @Test
    fun `updateEdgeDetails should return error on null response body`() = runTest {
        // Given
        val response = Response.success<UpdateEdgeResponse>(null)
        whenever(mockApiService.updateEdgeDetails(eq(spaceId), eq(edgeId), any())).thenReturn(response)

        // When
        val result = repository.updateEdgeDetails(spaceId, edgeId, "label", "source1", "target1", "")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error updating edge"))
    }

    @Test
    fun `deleteEdge should return success on deletion`() = runTest {
        // Given
        val mockResponse = DeleteEdgeResponse("Edge deleted successfully")
        val response = Response.success(mockResponse)
        whenever(mockApiService.deleteEdge(spaceId, edgeId)).thenReturn(response)

        // When
        val result = repository.deleteEdge(spaceId, edgeId)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        verify(mockApiService).deleteEdge(spaceId, edgeId)
    }

    @Test
    fun `deleteEdge should return error on failure`() = runTest {
        // Given
        val response = Response.error<DeleteEdgeResponse>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.deleteEdge(spaceId, edgeId)).thenReturn(response)

        // When
        val result = repository.deleteEdge(spaceId, edgeId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error deleting edge"))
    }

    @Test
    fun `deleteEdge should return error on null response body`() = runTest {
        // Given
        val response = Response.success<DeleteEdgeResponse>(null)
        whenever(mockApiService.deleteEdge(spaceId, edgeId)).thenReturn(response)

        // When
        val result = repository.deleteEdge(spaceId, edgeId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error deleting edge"))
    }

    @Test
    fun `deleteEdge should return error when not authenticated`() = runTest {
        // Given
        whenever(mockSessionManager.authToken).thenReturn(flowOf(null))

        // When
        val result = repository.deleteEdge(spaceId, edgeId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Not authenticated"))
    }

    @Test
    fun `searchWikidataEdgeLabels should return properties on success`() = runTest {
        // Given
        val query = "test"
        val mockResponse = listOf(
            WikidataPropertyDto("P1", "Property 1", "Description 1", "url1"),
            WikidataPropertyDto("P2", "Property 2", "Description 2", "url2")
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.searchWikidataProperties(query)).thenReturn(response)

        // When
        val result = repository.searchWikidataEdgeLabels(query)

        // Then
        assertTrue(result.isSuccess)
        val properties = result.getOrNull()!!
        assertEquals(2, properties.size)
        assertEquals("P1", properties[0].id)
        assertEquals("Property 1", properties[0].label)
        verify(mockApiService).searchWikidataProperties(query)
    }

    @Test
    fun `searchWikidataEdgeLabels should return error on failure`() = runTest {
        // Given
        val query = "test"
        val response = Response.error<List<WikidataPropertyDto>>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.searchWikidataProperties(query)).thenReturn(response)

        // When
        val result = repository.searchWikidataEdgeLabels(query)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Error searching properties"))
    }

    @Test
    fun `getSpaceEdges should handle exception`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        whenever(mockApiService.getSpaceEdges(spaceId)).thenThrow(exception)

        // When
        val result = repository.getSpaceEdges(spaceId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()!!.message)
    }

    @Test
    fun `addEdgeToSpaceGraph should handle exception`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        whenever(mockApiService.addEdgeToSpaceGraph(eq(spaceId), any())).thenThrow(exception)

        // When
        val result = repository.addEdgeToSpaceGraph(spaceId, "source1", "target1", "label", "")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()!!.message)
    }
}

