package com.yybb.myapplication.data.repository

import android.content.Context
import com.google.gson.JsonPrimitive
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.dto.AddEdgeResponse
import com.yybb.myapplication.data.network.dto.AddNodeRequest
import com.yybb.myapplication.data.network.dto.AddNodeResponse
import com.yybb.myapplication.data.network.dto.CreateSnapshotResponse
import com.yybb.myapplication.data.network.dto.DeleteEdgeResponse
import com.yybb.myapplication.data.network.dto.DeleteNodeResponse
import com.yybb.myapplication.data.network.dto.NodePropertyResponse
import com.yybb.myapplication.data.network.dto.SpaceEdgeResponse
import com.yybb.myapplication.data.network.dto.SpaceNodeResponse
import com.yybb.myapplication.data.network.dto.UpdateEdgeResponse
import com.yybb.myapplication.data.network.dto.UpdateNodePropertiesResponse
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

class SpaceNodeDetailsRepositoryTest {

    private lateinit var repository: SpaceNodeDetailsRepository
    private lateinit var mockContext: Context
    private lateinit var mockApiService: ApiService
    private lateinit var mockSessionManager: SessionManager

    private val spaceId = "space123"
    private val nodeId = "node456"
    private val token = "test_token"

    @Before
    fun setUp() {
        mockContext = mock()
        mockApiService = mock()
        mockSessionManager = mock()
        repository = SpaceNodeDetailsRepository(mockContext, mockApiService, mockSessionManager)

        whenever(mockSessionManager.authToken).thenReturn(flowOf(token))
        whenever(mockContext.getString(R.string.space_node_properties_error))
            .thenReturn("Error loading node properties")
        whenever(mockContext.getString(R.string.space_node_connections_error))
            .thenReturn("Error loading connections")
        whenever(mockContext.getString(R.string.space_nodes_error_message))
            .thenReturn("Error loading nodes")
        whenever(mockContext.getString(R.string.add_edge_property_search_error))
            .thenReturn("Error searching properties")
        whenever(mockContext.getString(R.string.space_node_update_properties_error))
            .thenReturn("Error updating properties")
        whenever(mockContext.getString(R.string.space_node_delete_property_error))
            .thenReturn("Error deleting property")
        whenever(mockContext.getString(R.string.add_edge_service_error))
            .thenReturn("Error adding edge")
        whenever(mockContext.getString(R.string.create_snapshot_service_error))
            .thenReturn("Error creating snapshot")
        whenever(mockContext.getString(R.string.delete_node_service_error))
            .thenReturn("Error deleting node")
        whenever(mockContext.getString(R.string.space_node_edit_properties_error))
            .thenReturn("Error loading properties")
        whenever(mockContext.getString(R.string.update_edge_service_error))
            .thenReturn("Error updating edge")
        whenever(mockContext.getString(R.string.delete_edge_service_error))
            .thenReturn("Error deleting edge")
    }

    @Test
    fun `getNodeProperties should return properties on success`() = runTest {
        // Given
        val mockResponse = listOf(
            NodePropertyResponse("stmt1", "P1", "Property 1", JsonPrimitive("Value 1"), "Property 1: Value 1")
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.getNodeProperties(spaceId, nodeId)).thenReturn(response)

        // When
        val result = repository.getNodeProperties(spaceId, nodeId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        verify(mockApiService).getNodeProperties(spaceId, nodeId)
    }

    @Test
    fun `getNodeProperties should return error on failure`() = runTest {
        // Given
        val response = Response.error<List<NodePropertyResponse>>(
            500,
            "Error".toResponseBody()
        )
        whenever(mockApiService.getNodeProperties(spaceId, nodeId)).thenReturn(response)

        // When
        val result = repository.getNodeProperties(spaceId, nodeId)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getNodeProperties should return error when not authenticated`() = runTest {
        // Given
        whenever(mockSessionManager.authToken).thenReturn(flowOf(null))

        // When
        val result = repository.getNodeProperties(spaceId, nodeId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Not authenticated"))
    }

    @Test
    fun `getSpaceEdges should return edges on success`() = runTest {
        // Given
        val mockResponse = listOf(
            SpaceEdgeResponse(1, 1, 2, "Edge 1", null)
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.getSpaceEdges(spaceId)).thenReturn(response)

        // When
        val result = repository.getSpaceEdges(spaceId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
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
    }

    @Test
    fun `getSpaceNodes should return nodes on success`() = runTest {
        // Given
        val mockResponse = listOf(
            SpaceNodeResponse(1, "Node 1", null, null, null, null, null, null, null, null)
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.getSpaceNodes(spaceId)).thenReturn(response)

        // When
        val result = repository.getSpaceNodes(spaceId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        verify(mockApiService).getSpaceNodes(spaceId)
    }

    @Test
    fun `searchWikidataProperties should return properties on success`() = runTest {
        // Given
        val query = "test"
        val mockResponse = listOf(
            WikidataPropertyDto("P1", "Property 1", "Description 1", "url1")
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.searchWikidataProperties(query)).thenReturn(response)

        // When
        val result = repository.searchWikidataProperties(query)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        verify(mockApiService).searchWikidataProperties(query)
    }

    @Test
    fun `searchWikidataEntities should return entities on success`() = runTest {
        // Given
        val query = "test"
        val mockResponse = listOf(
            WikidataPropertyDto("Q1", "Entity 1", "Description 1", "url1")
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.searchWikidataEntities(query)).thenReturn(response)

        // When
        val result = repository.searchWikidataEntities(query)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        verify(mockApiService).searchWikidataEntities(query)
    }

    @Test
    fun `searchWikidataEdgeLabels should call searchWikidataProperties`() = runTest {
        // Given
        val query = "test"
        val mockResponse = listOf(
            WikidataPropertyDto("P1", "Property 1", "Description 1", "url1")
        )
        val response = Response.success(mockResponse)
        whenever(mockApiService.searchWikidataProperties(query)).thenReturn(response)

        // When
        val result = repository.searchWikidataEdgeLabels(query)

        // Then
        assertTrue(result.isSuccess)
        verify(mockApiService).searchWikidataProperties(query)
    }

    @Test
    fun `updateNodeProperties should return success on update`() = runTest {
        // Given
        val properties = listOf(
            NodeProperty("stmt1", "P1", "Property 1", "Value 1", false, null, "Property 1: Value 1")
        )
        val mockResponse = UpdateNodePropertiesResponse("Properties updated successfully")
        val response = Response.success(mockResponse)
        whenever(mockApiService.updateNodeProperties(eq(spaceId), eq(nodeId), any())).thenReturn(response)

        // When
        val result = repository.updateNodeProperties(spaceId, nodeId, properties)

        // Then
        assertTrue(result.isSuccess)
        verify(mockApiService).updateNodeProperties(eq(spaceId), eq(nodeId), any())
    }

    @Test
    fun `addEdgeToSpaceGraph should return edge response on success`() = runTest {
        // Given
        val sourceId = "source1"
        val targetId = "target1"
        val label = "Edge Label"
        val wikidataPropertyId = "P1"
        val mockResponse = AddEdgeResponse("Edge added", 1)
        val response = Response.success(mockResponse)
        whenever(mockApiService.addEdgeToSpaceGraph(eq(spaceId), any())).thenReturn(response)

        // When
        val result = repository.addEdgeToSpaceGraph(spaceId, sourceId, targetId, label, wikidataPropertyId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.edgeId)
        verify(mockApiService).addEdgeToSpaceGraph(eq(spaceId), any())
    }

    @Test
    fun `createSnapshot should return snapshot response on success`() = runTest {
        // Given
        val mockResponse = CreateSnapshotResponse(1, "2024-01-01")
        val response = Response.success(mockResponse)
        whenever(mockApiService.createSnapshot(eq(spaceId), any())).thenReturn(response)

        // When
        val result = repository.createSnapshot(spaceId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.snapshotId)
        verify(mockApiService).createSnapshot(eq(spaceId), any())
    }

    @Test
    fun `deleteNode should return delete response on success`() = runTest {
        // Given
        val mockResponse = DeleteNodeResponse("Node deleted")
        val response = Response.success(mockResponse)
        whenever(mockApiService.deleteNode(spaceId, nodeId)).thenReturn(response)

        // When
        val result = repository.deleteNode(spaceId, nodeId)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        verify(mockApiService).deleteNode(spaceId, nodeId)
    }

    @Test
    fun `addNode should return node response on success`() = runTest {
        // Given
        val request = AddNodeRequest(
            relatedNodeId = null,
            wikidataEntity = com.yybb.myapplication.data.network.dto.AddNodeWikidataEntity(
                "Q1", "Entity 1", "Description", "url", null
            ),
            edgeLabel = "",
            isNewNodeSource = true,
            location = com.yybb.myapplication.data.network.dto.AddNodeLocation(),
            selectedProperties = emptyList()
        )
        val mockResponse = AddNodeResponse("Node created")
        val response = Response.success(mockResponse)
        whenever(mockApiService.addNode(spaceId, request)).thenReturn(response)

        // When
        val result = repository.addNode(spaceId, request)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        verify(mockApiService).addNode(spaceId, request)
    }

    @Test
    fun `updateEdgeDetails should return success on update`() = runTest {
        // Given
        val edgeId = "edge1"
        val label = "Updated Label"
        val sourceId = "source1"
        val targetId = "target1"
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
    fun `deleteEdge should return success on deletion`() = runTest {
        // Given
        val edgeId = "edge1"
        val mockResponse = DeleteEdgeResponse("Edge deleted")
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
    fun `getNodeProperties should handle null response body`() = runTest {
        // Given
        val response = Response.success<List<NodePropertyResponse>>(null)
        whenever(mockApiService.getNodeProperties(spaceId, nodeId)).thenReturn(response)

        // When
        val result = repository.getNodeProperties(spaceId, nodeId)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `addEdgeToSpaceGraph should handle null response body`() = runTest {
        // Given
        val response = Response.success<AddEdgeResponse>(null)
        whenever(mockApiService.addEdgeToSpaceGraph(eq(spaceId), any())).thenReturn(response)

        // When
        val result = repository.addEdgeToSpaceGraph(spaceId, "source1", "target1", "label", "")

        // Then
        assertTrue(result.isFailure)
    }
}

