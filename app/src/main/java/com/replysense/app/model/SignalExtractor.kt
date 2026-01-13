package com.replysense.app.model

object SignalExtractor {

    fun extractSignals(turns: List<ConversationTurn>): Set<EmotionalSignal> {
        val signals = mutableSetOf<EmotionalSignal>()
        val otherTurns = turns.filter { it.speaker == Speaker.OTHER }

        if (otherTurns.any { it.text.length < 25 }) {
            signals += EmotionalSignal.DISENGAGEMENT
        }

        if (otherTurns.any { it.text.contains("maybe", true) }) {
            signals += EmotionalSignal.CAUTION
        }

        if (otherTurns.any { it.text.contains("I care", true) }) {
            signals += EmotionalSignal.CARE
        }

        if (otherTurns.any { it.text.contains("I don't know", true) }) {
            signals += EmotionalSignal.CONFUSION
        }

        return signals
    }

    fun trajectory(turns: List<ConversationTurn>): ConversationTrajectory {
        val lengths = turns.map { it.text.length }
        return if (lengths.zipWithNext().all { it.second <= it.first }) {
            ConversationTrajectory.STAGNATING
        } else {
            ConversationTrajectory.IMPROVING
        }
    }
}
