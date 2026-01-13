package com.replysense.app.domain.core

/**
 * Canonical domain models.
 * SINGLE source of truth. No duplicates anywhere else.
 */

enum class Emotion {
    CALM,
    INTERESTED,
    UNCERTAIN,
    DISTRESSED,
    ANGRY
}

enum class EmotionSeverity {
    LOW,
    MEDIUM,
    HIGH
}

enum class AttractionSignal {
    NONE,
    INTEREST,
    SEXUAL_INTEREST
}

enum class RedFlag {
    PRESSURE,
    DISRESPECT,
    MANIPULATION,
    BOUNDARY_PUSH
}

enum class ConversationContext {
    CASUAL,
    FLIRTING,
    PROFESSIONAL,
    CONFLICT
}
