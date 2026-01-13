package com.replysense.domain.evaluation

data class EvaluationScores(
    val emotionalAccuracy: Float,
    val intentConfidence: Float,
    val escalationRisk: Float,
    val alignmentWithUserGoal: Float,
    val ambiguity: Float
)
