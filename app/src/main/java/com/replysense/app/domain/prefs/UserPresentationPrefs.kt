package com.replysense.app.domain.prefs

data class UserPresentationPrefs(
    val optedIn: Boolean,
    val tone: CoachingTone = CoachingTone.BALANCED,
    val density: ExplanationDensity = ExplanationDensity.STANDARD
)
