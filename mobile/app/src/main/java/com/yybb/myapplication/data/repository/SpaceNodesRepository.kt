package com.yybb.myapplication.data.repository

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.model.SpaceNode
import com.yybb.myapplication.data.model.SpaceEdge
import com.yybb.myapplication.data.model.toSpaceNode
import com.yybb.myapplication.data.model.toSpaceEdge
import com.yybb.myapplication.data.network.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpaceNodesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

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
}


