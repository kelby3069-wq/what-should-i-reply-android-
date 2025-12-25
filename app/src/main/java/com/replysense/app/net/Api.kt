package com.replysense.app.net

import com.replysense.app.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Api {
    private val client = OkHttpClient()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun buildReplyRequest(message: String, vibe: String?, context: String?): ReplyRequest {
        return ReplyRequest(message = message, vibe = vibe, context = context)
    }

    /**
     * Synchronous call (simple + reliable for MVP).
     * You can wrap it in Dispatchers.IO later.
     */
    fun postReply(req: ReplyRequest): ReplyResponse {
        val url = BuildConfig.API_BASE_URL.trimEnd('/') + "/reply"

        val bodyJson = json.encodeToString(req)
        val body = bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())

        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)

        val key = BuildConfig.API_KEY
        if (key.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $key")
        }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            return if (resp.isSuccessful) {
                runCatching { json.decodeFromString(ReplyResponse.serializer(), raw) }
                    .getOrElse { ReplyResponse(error = "Parse error", details = it.message) }
            } else {
                ReplyResponse(error = "HTTP ${resp.code}", details = raw.take(3000))
            }
        }
    }
}
