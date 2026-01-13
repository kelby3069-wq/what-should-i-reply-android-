package com.replysense.app.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiService {

    @Headers(
        "Content-Type: application/json"
    )
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: OpenAiRequest
    ): OpenAiResponse
}
