package com.replysense.app

object AppConfig {
    // Comes from BuildConfig so CI doesn't explode if you haven't set secrets.
    val baseUrl: String = BuildConfig.API_BASE_URL
    val apiKey: String = BuildConfig.API_KEY

    fun hasApiKey(): Boolean = apiKey.isNotBlank()
}
