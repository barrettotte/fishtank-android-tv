package com.barrettotte.fishtank.data.repository

import com.barrettotte.fishtank.data.api.FishtankApi
import com.barrettotte.fishtank.data.model.LiveStream
import com.barrettotte.fishtank.util.Logger

/** Holds parsed live stream data from the API. */
data class StreamData(
    val streams: List<LiveStream>,
    val statusMap: Map<String, String>,
    val loadBalancerMap: Map<String, String>,
)

/** Repository for fetching live stream data and load balancer info. */
class StreamRepository(private val api: FishtankApi) {
    companion object {
        private const val TAG = "Stream"
    }

    /** Fetch all live streams. Returns null on auth failure (401/403) for redirect handling. */
    suspend fun fetchStreams(): Result<StreamData> {
        return try {
            Logger.d(TAG, "Fetching live streams...")
            val response = api.getLiveStreams()

            if (response.code() == 401 || response.code() == 403) {
                Logger.e(TAG, "Token expired: ${response.code()}")
                return Result.failure(TokenExpiredException())
            }

            if (!response.isSuccessful) {
                Logger.e(TAG, "Fetch streams failed: ${response.code()}")
                return Result.failure(Exception("Failed to fetch streams: ${response.code()}"))
            }

            val body = response.body()
            if (body == null) {
                Logger.e(TAG, "Streams response body is null")
                return Result.failure(Exception("Empty response"))
            }

            // Handle both flat and nested formats
            val streams = body.liveStreams ?: body.data?.liveStreams ?: emptyList()
            val statusMap = body.liveStreamStatus ?: body.data?.liveStreamStatus ?: emptyMap()
            val loadBalancerMap = body.loadBalancer ?: body.data?.loadBalancer ?: emptyMap()

            Logger.d(TAG, "Fetched ${streams.size} streams, ${statusMap.count { it.value == "online" }} online")

            Result.success(StreamData(streams, statusMap, loadBalancerMap))
        } catch (e: Exception) {
            Logger.e(TAG, "Fetch streams exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}

/** Thrown when the API returns 401/403, indicating the token has expired. */
class TokenExpiredException : Exception("Token expired")
