package com.replysense.domain.model

data class UserMessage(
    val rawText: String,
    val source: MessageSource
)

enum class MessageSource {
    TEXT_INPUT,
    SCREENSHOT_OCR
}
