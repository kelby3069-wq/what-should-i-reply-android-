package com.replysense.app.domain.emotion

object ContextClassifier {

    fun detect(text: String): ConversationContext {
        val lower = text.lowercase()

        return when {
            lower.contains("shift") ||
                    lower.contains("schedule") ||
                    lower.contains("meeting") ||
                    lower.contains("urgent:") -> ConversationContext.WORKPLACE

            lower.contains("regards") ||
                    lower.contains("please") ||
                    lower.contains("thank you") -> ConversationContext.PROFESSIONAL

            lower.contains("lol") ||
                    lower.contains("btw") ||
                    lower.contains("iykyk") ||
                    text == text.lowercase() -> ConversationContext.YOUTH

            else -> ConversationContext.PERSONAL
        }
    }
}
