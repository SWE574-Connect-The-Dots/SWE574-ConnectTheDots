package com.yybb.myapplication.data.repository

import android.content.Context
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.SpaceEdge
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.WikidataProperty
import com.yybb.myapplication.data.model.toNodeProperty
import com.yybb.myapplication.data.model.toSpaceEdge
import com.yybb.myapplication.data.model.toSpaceNode
import com.yybb.myapplication.data.network.dto.toWikidataProperty
import com.yybb.myapplication.data.network.ApiService
import com.yybb.myapplication.data.network.NominatimApiService
import com.yybb.myapplication.data.network.dto.AddEdgeRequest
import com.yybb.myapplication.data.network.dto.AddEdgeResponse
import com.yybb.myapplication.data.network.dto.AddNodeRequest
import com.yybb.myapplication.data.network.dto.AddNodeResponse
import com.yybb.myapplication.data.network.dto.CreateSnapshotResponse
import com.yybb.myapplication.data.network.dto.DeleteNodeResponse
import com.yybb.myapplication.data.network.dto.DeleteEdgeResponse
import com.yybb.myapplication.data.network.dto.NominatimCoordinates
import com.yybb.myapplication.data.network.dto.UpdateEdgeRequest
import com.yybb.myapplication.data.network.dto.UpdateEdgeResponse
import com.yybb.myapplication.data.network.dto.UpdateNodeLocationRequest
import com.yybb.myapplication.data.network.dto.UpdateNodeLocationResponse
import com.yybb.myapplication.data.network.dto.UpdateNodePropertiesRequest
import com.yybb.myapplication.data.network.dto.UpdateNodePropertyItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpaceNodeDetailsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val nominatimApiService: NominatimApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getNodeProperties(spaceId: String, nodeId: String): Result<List<NodeProperty>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.getNodeProperties(spaceId, nodeId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body.map { it.toNodeProperty() })
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.space_node_properties_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.space_node_properties_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSpaceEdges(spaceId: String): Result<List<SpaceEdge>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.getSpaceEdges(spaceId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body.map { it.toSpaceEdge() })
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.space_node_connections_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.space_node_connections_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSpaceNodes(spaceId: String): Result<List<SpaceNode>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.getSpaceNodes(spaceId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body.map { it.toSpaceNode() })
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.space_nodes_error_message))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.space_nodes_error_message)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchWikidataProperties(query: String): Result<List<WikidataProperty>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.searchWikidataProperties(query)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body.map { it.toWikidataProperty() })
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.add_edge_property_search_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.add_edge_property_search_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchWikidataEntities(query: String): Result<List<WikidataProperty>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.searchWikidataEntities(query)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body.map { it.toWikidataProperty() })
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.add_edge_property_search_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.add_edge_property_search_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchWikidataEdgeLabels(query: String): Result<List<WikidataProperty>> {
        return searchWikidataProperties(query)
    }

    suspend fun updateNodeProperties(
        spaceId: String,
        nodeId: String,
        properties: List<NodeProperty>
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val requestItems = properties.map { property ->
                    val valueElement = when {
                        property.isEntity -> JsonObject().apply {
                            addProperty("type", "entity")
                            property.entityId?.let { addProperty("id", it) }
                            addProperty("text", property.valueText)
                        }

                        property.valueText.isNotBlank() -> JsonPrimitive(property.valueText)

                        else -> JsonNull.INSTANCE
                    }

                    UpdateNodePropertyItem(
                        statementId = property.statementId,
                        property = property.propertyId,
                        propertyLabel = property.propertyLabel,
                        value = valueElement
                    )
                }

                val request = UpdateNodePropertiesRequest(selectedProperties = requestItems)
                val response = apiService.updateNodeProperties(spaceId, nodeId, request)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.space_node_update_properties_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteNodeProperty(
        spaceId: String,
        nodeId: String,
        statementId: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.deleteNodeProperty(spaceId, nodeId, statementId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.space_node_delete_property_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addEdgeToSpaceGraph(
        spaceId: String,
        sourceId: String,
        targetId: String,
        label: String,
        wikidataPropertyId: String
    ): Result<AddEdgeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val request = AddEdgeRequest(
                    sourceId = sourceId,
                    targetId = targetId,
                    label = label,
                    wikidataPropertyId = wikidataPropertyId
                )

                val response = apiService.addEdgeToSpaceGraph(spaceId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.add_edge_service_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.add_edge_service_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createSnapshot(spaceId: String): Result<CreateSnapshotResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.createSnapshot(spaceId, emptyMap())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.create_snapshot_service_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.create_snapshot_service_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteNode(spaceId: String, nodeId: String): Result<DeleteNodeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.deleteNode(spaceId, nodeId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.delete_node_service_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.delete_node_service_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getWikidataEntityProperties(entityId: String): Result<List<NodeProperty>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.getWikidataPropertiesNode(entityId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body.map { it.toNodeProperty() })
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.space_node_edit_properties_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.space_node_edit_properties_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addNode(
        spaceId: String,
        request: AddNodeRequest
    ): Result<AddNodeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.addNode(spaceId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(
                            Exception("Failed to create node: Empty response")
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "Failed to create node: ${response.errorBody()?.string()}"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateEdgeDetails(
        spaceId: String,
        edgeId: String,
        label: String,
        sourceId: String,
        targetId: String,
        wikidataPropertyId: String
    ): Result<UpdateEdgeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val request = UpdateEdgeRequest(
                    label = label,
                    sourceId = sourceId,
                    targetId = targetId,
                    wikidataPropertyId = wikidataPropertyId
                )

                val response = apiService.updateEdgeDetails(spaceId, edgeId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.update_edge_service_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.update_edge_service_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteEdge(
        spaceId: String,
        edgeId: String
    ): Result<DeleteEdgeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.deleteEdge(spaceId, edgeId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.delete_edge_service_error))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "${context.getString(R.string.delete_edge_service_error)}: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCoordinatesFromAddress(query: String): Result<NominatimCoordinates> {
        return withContext(Dispatchers.IO) {
            try {
                val response = nominatimApiService.search(
                    query = query,
                    format = "json",
                    addressdetails = 1,
                    limit = 3,
                    acceptLanguage = "en"
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isNotEmpty()) {
                        val firstResult = body[0]
                        val lat = firstResult.lat.toDoubleOrNull()
                        val lon = firstResult.lon.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            Result.success(
                                NominatimCoordinates(
                                    displayName = firstResult.displayName,
                                    latitude = lat,
                                    longitude = lon
                                )
                            )
                        } else {
                            Result.failure(Exception("Invalid coordinates in response"))
                        }
                    } else {
                        Result.failure(Exception("No results found"))
                    }
                } else {
                    Result.failure(
                        Exception("Failed to get coordinates: ${response.errorBody()?.string()}")
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateNodeLocation(
        spaceId: String,
        nodeId: String,
        country: String?,
        city: String?,
        locationName: String?,
        latitude: Double?,
        longitude: Double?
    ): Result<UpdateNodeLocationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val request = UpdateNodeLocationRequest(
                    country = country,
                    city = city,
                    district = null,
                    street = null,
                    latitude = latitude,
                    longitude = longitude,
                    locationName = locationName
                )

                val response = apiService.updateNodeLocation(spaceId, nodeId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(
                            Exception("Failed to update location: Empty response")
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "Failed to update location: ${response.errorBody()?.string()}"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}


