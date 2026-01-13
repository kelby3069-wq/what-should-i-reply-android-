package com.replysense.domain.memory

data class AgentMemory(
    val userPreferences: Map<String, Any> = emptyMap(),
    val behavioralPatterns: Map<String, Float> = emptyMap(),
    val historicalOutcomes: List<OutcomeRecord> = emptyList()
)

data class OutcomeRecord(
    val pattern: String,
    val outcomeScore: Float
)
