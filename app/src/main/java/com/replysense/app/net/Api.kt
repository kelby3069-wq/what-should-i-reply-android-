package com.replysense.app.net

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object Api {
    // Your Cloudflare Worker
    private const val BASE_URL = "https://reply-sense.kelby3069.workers.dev"
    private const val ENDPOINT = "/reply"

    private val client = OkHttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun buildRequestBody(payload: ReplyRequest): String =
        json.encodeToString(ReplyRequest.serializer(), payload)

    fun parseResponse(body: String): ReplyResponse =
        json.decodeFromString(ReplyResponse.serializer(), body)

    suspend fun generateReplies(payload: ReplyRequest): Result<ReplyResponse> {
        return kotlin.runCatching {
            val bodyStr = buildRequestBody(payload)
            val req = Request.Builder()
                .url(BASE_URL.trimEnd('/') + ENDPOINT)
                .post(bodyStr.toRequestBody(jsonMediaType))
                .build()

            val resp = client.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()

            if (!resp.isSuccessful) {
                // Try parse for better error, otherwise raw
                val parsed = runCatching { parseResponse(respBody) }.getOrNull()
                throw RuntimeException(parsed?.details ?: parsed?.error ?: "HTTP ${resp.code}: $respBody")
            }

            parseResponse(respBody)
        }
    }
}
