package com.replysense.app.domain.intelligence.features

import kotlin.math.abs
import kotlin.math.max

/**
 * Pure, deterministic feature extraction for Phase C.2.
 * No inference. No fallback. No nulls.
 */
class PhaseCFeatureExtractor {

    fun extract(lines: List<String>): PhaseCFeatures {
        val safeLines = lines.map { it.trim() }.filter { it.isNotEmpty() }
        val messageCount = safeLines.size

        val lengths = safeLines.map { it.length }
        val totalChars = max(1, lengths.sum())

        val averageLength =
            if (lengths.isNotEmpty()) lengths.sum() / lengths.size else 0
        val longestLength =
            lengths.maxOrNull() ?: 0

        val abruptChanges = lengths.zipWithNext { a, b -> abs(b - a) }
        val abruptIndex =
            if (abruptChanges.isNotEmpty()) abruptChanges.average().toFloat() else 0f

        val exclamations = safeLines.sumOf { countChar(it, '!') }
        val questions = safeLines.sumOf { countChar(it, '?') }
        val capsCount = safeLines.sumOf { it.count(Char::isUpperCase) }

        val emotionalBursts = safeLines.count {
            it.contains("!!") || it.contains("?!") || it.contains("...")
        }

        val withdrawalHits = safeLines.count {
            it.containsAny(
                "i'm done",
                "whatever",
                "fine",
                "leave me alone",
                "doesn't matter"
            )
        }

        val blameHits = safeLines.count {
            it.containsAny(
                "you always",
                "you never",
                "your fault",
                "because of you"
            )
        }

        val reassuranceHits = safeLines.count {
            it.containsAny(
                "do you even care",
                "do you care",
                "why don't you",
                "am i important"
            )
        }

        val commandHits = safeLines.count {
            it.containsAny(
                "stop",
                "don't",
                "you need to",
                "you have to"
            )
        }

        val apologyPresent = safeLines.any {
            it.containsAny(
                "sorry",
                "i apologize",
                "my fault"
            )
        }

        val repairPresent = safeLines.any {
            it.containsAny(
                "can we talk",
                "let's fix this",
                "i want to understand",
                "work this out"
            )
        }

        val deflectionHits = safeLines.count {
            it.containsAny(
                "anyway",
                "that's not the point",
                "forget it"
            )
        }

        val halfIndex = messageCount / 2
        val earlyIntensity = safeLines.take(halfIndex).sumOf {
            countChar(it, '!') + countChar(it, '?')
        }
        val lateIntensity = safeLines.drop(halfIndex).sumOf {
            countChar(it, '!') + countChar(it, '?')
        }

        val escalationSlope =
            if (earlyIntensity == 0) {
                lateIntensity.toFloat()
            } else {
                (lateIntensity - earlyIntensity).toFloat() / earlyIntensity
            }

        val lateStageNegativityBias =
            if (earlyIntensity + lateIntensity == 0) 0f
            else lateIntensity.toFloat() / (earlyIntensity + lateIntensity)

        val ruptureScore =
            (withdrawalHits * 1.5f) +
                    (blameHits * 1.0f) +
                    (deflectionHits * 0.5f) +
                    (if (lateStageNegativityBias > 0.6f) 1.0f else 0f)

        return PhaseCFeatures(
            messageCount = messageCount,
            averageMessageLength = averageLength,
            longestMessageLength = longestLength,
            backAndForthRatio = estimateBackAndForth(messageCount),
            monologueFlag = longestLength > averageLength * 3,

            exclamationDensity = exclamations.toFloat() / totalChars,
            questionDensity = questions.toFloat() / totalChars,
            capitalizationRate = capsCount.toFloat() / totalChars,
            abruptLengthChangeIndex = abruptIndex,
            emotionalPunctuationBursts = emotionalBursts,

            withdrawalLanguageHits = withdrawalHits,
            blameLanguageHits = blameHits,
            reassuranceSeekingHits = reassuranceHits,
            escalationSlope = escalationSlope,
            ruptureScore = ruptureScore,

            commandLanguageHits = commandHits,
            apologyPresent = apologyPresent,
            repairAttemptPresent = repairPresent,
            deflectionMarkers = deflectionHits,

            lateStageNegativityBias = lateStageNegativityBias
        )
    }

    private fun estimateBackAndForth(messageCount: Int): Float =
        if (messageCount <= 1) 0f else (messageCount - 1).toFloat() / messageCount

    private fun countChar(text: String, char: Char): Int =
        text.count { it == char }

    private fun String.containsAny(vararg phrases: String): Boolean {
        val lower = this.lowercase()
        return phrases.any { lower.contains(it) }
    }
}
