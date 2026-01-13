package com.replysense.app.domain

enum class RedFlagType {
    GHOSTING,
    HOT_COLD,
    PRESSURE,
    DISRESPECT,
    BOUNDARY_ISSUE
}

data class RedFlag(
    val type: RedFlagType,
    val description: String,
    val evidence: String
)

data class ResponsibilityAttribution(
    val userDidWell: List<String>,
    val userDidNotCause: List<String>,
    val otherPartyActions: List<String>
)
