package com.replysense.app.domain.intelligence.validation

/**
 * Represents a detected conflict between signals.
 * INTERNAL ONLY.
 */
data class ConflictSignal(
    val type: ConflictType,
    val description: String,
    val messageIndices: List<Int> = emptyList()
)

enum class ConflictType {
    AFFECT_BEHAVIOR_MISMATCH,
    APOLOGY_WITH_BLAME,
    REPAIR_WITH_ESCALATION,
    WARMTH_WITH_WITHDRAWAL,
    SIGNAL_REVERSAL
}
