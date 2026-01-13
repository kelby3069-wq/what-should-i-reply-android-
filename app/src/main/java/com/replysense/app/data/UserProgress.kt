package com.replysense.app.data

data class UserProgress(
    val userScore: Int,
    val trendDelta: Int,
    val streak: Int,
    val weakestDimension: String
)
