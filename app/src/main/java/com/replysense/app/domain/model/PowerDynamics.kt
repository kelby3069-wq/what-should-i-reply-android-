package com.replysense.app.domain.model

data class PowerDynamics(
    val isImbalanced: Boolean = false,
    val dominantSide: DominantSide = DominantSide.NONE
)

enum class DominantSide {
    USER,
    OTHER,
    NONE
}
