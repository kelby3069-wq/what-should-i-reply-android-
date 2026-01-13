package com.replysense.app.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class OpenAiClient(
    private val apiKey: String
) {

    private val client = OkHttpClient()

    private val jsonMediaType = "application/json".toMediaType()

    suspend fun createChatCompletion(
        model: String,
        messages: List<Pair<String, String>>,
        temperature: Double
    ): String {

        val bodyJson = JSONObject().apply {
            put("model", model)
            put("temperature", temperature)
            put(
                "messages",
                messages.map { (role, content) ->
                    JSONObject().apply {
                        put("role", role)
                        put("content", content)
                    }
                }
            )
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("OpenAI error: ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: error("Empty OpenAI response")

            val json = JSONObject(responseBody)
            return json
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }
}
