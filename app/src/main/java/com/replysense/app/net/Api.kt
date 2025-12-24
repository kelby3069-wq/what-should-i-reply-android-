package com.replysense.app.net

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object Api {
    private const val BASE_URL = "https://reply-sense.kelby3069.workers.dev"
    private const val ENDPOINT = "/reply"

    private val client = OkHttpClient()

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateReplies(payload: ReplyRequest): Result<ReplyResponse> {
        return kotlin.runCatching {
            val bodyStr = json.encodeToString(ReplyRequest.serializer(), payload)
            val req = Request.Builder()
                .url(BASE_URL.trimEnd('/') + ENDPOINT)
                .post(bodyStr.toRequestBody(jsonMediaType))
                .build()

            val resp = client.newCall(req).execute()
            val respBody = resp.body?.string().orEmpty()

            if (!resp.isSuccessful) {
                val parsed = runCatching {
                    json.decodeFromString(ReplyResponse.serializer(), respBody)
                }.getOrNull()
                throw RuntimeException(parsed?.details ?: parsed?.error ?: "HTTP ${resp.code}: $respBody")
            }

            json.decodeFromString(ReplyResponse.serializer(), respBody)
        }
    }
}
