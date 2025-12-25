package com.replysense.app

object AppConfig {
    // Explicitly qualify BuildConfig so there’s no import/package ambiguity.
    val isDebug: Boolean = com.replysense.app.BuildConfig.DEBUG
    val applicationId: String = com.replysense.app.BuildConfig.APPLICATION_ID
}
