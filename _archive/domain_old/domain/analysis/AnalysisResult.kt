package com.replysense.app.domain.analysis

import com.replysense.app.domain.reply.ReplyTone

data class AnalysisResult(
    val sexualContext: SexualContext,
    val emotionalSummary: EmotionalSummary,
    val keyMoment: KeyMoment?,
    val userContribution: UserContribution,
    val recoverability: Recoverability,
    val allowSexualReplyHelp: Boolean,
    val enforcedReplyTone: ReplyTone?,
    val requiresDeepAnalysis: Boolean
)

data class EmotionalSummary(
    val userState: EmotionalState,
    val otherState: EmotionalState,
    val trajectory: ConversationTrajectory
)

data class KeyMoment(
    val messageIndex: Int,
    val excerpt: String,
    val explanation: String
)

enum class UserContribution {
    NONE,
    MINOR,
    SIGNIFICANT
}

enum class Recoverability {
    HIGH,
    MEDIUM,
    LOW
}

enum class EmotionalState {
    CALM,
    TENSE,
    DEFENSIVE,
    OVERWHELMED,
    DISTRESSED
}

enum class ConversationTrajectory {
    IMPROVING,
    STABLE,
    ESCALATING
}
