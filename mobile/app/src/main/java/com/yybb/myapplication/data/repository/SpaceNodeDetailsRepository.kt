package com.yybb.myapplication.data.repository

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.model.NodeProperty
import com.yybb.myapplication.data.model.toNodeProperty
import com.yybb.myapplication.data.network.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpaceNodeDetailsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
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
}


