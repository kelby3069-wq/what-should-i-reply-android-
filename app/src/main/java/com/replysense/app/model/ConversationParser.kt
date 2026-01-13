package com.replysense.app.model

object ConversationParser {

    fun parse(raw: String): List<ConversationTurn> {
        return raw
            .lines()
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                val isUser = line.startsWith("You:", ignoreCase = true)
                ConversationTurn(
                    speaker = if (isUser) Speaker.USER else Speaker.OTHER,
                    text = line.removePrefix("You:").trim(),
                    index = index
                )
            }
    }
}
