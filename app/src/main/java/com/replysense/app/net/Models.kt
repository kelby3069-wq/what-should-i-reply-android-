package com.replysense.app.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReplyRequest(
    val message: String,
    val vibe: String? = null,
    val context: String? = null
)

@Serializable
data class ReplyOption(
    val text: String,
    val tone: String? = null,
    val label: String? = null
)

@Serializable
data class ReplyResponse(
    val options: List<ReplyOption> = emptyList(),
    val error: String? = null,
    val details: String? = null
)
