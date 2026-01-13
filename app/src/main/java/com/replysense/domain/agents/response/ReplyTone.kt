package com.replysense.app.model.enums

/**
 * Canonical reply tone definitions.
 * These describe HOW to respond, not WHAT to say.
 */
enum class ReplyTone(
    val displayName: String,
    val description: String,
    val recommended: Boolean = false
) {
    CALM(
        displayName = "Calm",
        description = "Grounded, steady responses that reduce emotional intensity and restore safety.",
        recommended = true
    ),

    SUPPORTIVE(
        displayName = "Supportive",
        description = "Affirms feelings without taking blame or escalating emotion."
    ),

    DIRECT(
        displayName = "Direct",
        description = "Clear and honest, but may escalate if emotions are still high."
    )
}
