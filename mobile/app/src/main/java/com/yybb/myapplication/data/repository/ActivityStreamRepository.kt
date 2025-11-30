package com.yybb.myapplication.data.repository

import android.content.Context
import com.yybb.myapplication.R
import com.yybb.myapplication.data.SessionManager
import com.yybb.myapplication.data.model.Activity
import com.yybb.myapplication.data.model.toActivity
import com.yybb.myapplication.data.network.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityStreamRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) {
    suspend fun getActivityStream(limit: Int = 100, since: String? = null): Result<List<Activity>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token == null) {
                    throw Exception("Not authenticated")
                }

                val response = apiService.getActivityStream(limit, since)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body.orderedItems.map { it.toActivity() })
                    } else {
                        Result.failure(
                            Exception(context.getString(R.string.loading_message))
                        )
                    }
                } else {
                    Result.failure(
                        Exception(
                            "Failed to load activity stream: ${
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
