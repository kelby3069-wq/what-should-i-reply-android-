package com.replysense.app.domain

data class ReplySuggestion(
    val tone: ReplyTone,
    val text: String,
    val rationale: String
)
