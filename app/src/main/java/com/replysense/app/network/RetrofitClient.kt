package com.replysense.app.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    fun createOpenAiService(context: Context): OpenAiService? {
        val apiKey = getApiKey(context) ?: return null

        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(OpenAiService::class.java)
    }

    private fun getApiKey(context: Context): String? {
        return try {
            val properties = java.util.Properties()
            val inputStream = context.assets.open("local.properties")
            properties.load(inputStream)
            properties.getProperty("OPENAI_API_KEY")
        } catch (e: Exception) {
            null
        }
    }
}
