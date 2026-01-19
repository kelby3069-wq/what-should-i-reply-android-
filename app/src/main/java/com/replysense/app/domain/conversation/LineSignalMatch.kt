package com.replysense.app.domain.conversation

/**
 * Maps a detected signal to a specific conversation line.
 */
data class LineSignalMatch(
    val lineId: Int,
    val signalType: SignalType
)
