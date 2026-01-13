package com.replysense.app.domain.analysis

import com.replysense.app.domain.reply.ReplyTone
import com.replysense.app.domain.safety.SexualSafetyGate

class ConversationAnalyzer(
    private val sexualContextDetector: SexualContextDetector = SexualContextDetector(),
    private val safetyGate: SexualSafetyGate = SexualSafetyGate()
) {

    fun analyze(messages: List<String>): AnalysisResult {
        val sexualContext = sexualContextDetector.detect(messages)

        val allowReplyHelp = safetyGate.allowSexualReply(sexualContext)
        val enforcedTone =
            if (!allowReplyHelp) ReplyTone.PROTECTIVE else null

        return AnalysisResult(
            sexualContext = sexualContext,
            emotionalSummary = EmotionalSummary(
                userState = EmotionalState.TENSE,
                otherState = EmotionalState.TENSE,
                trajectory = ConversationTrajectory.STABLE
            ),
            keyMoment = null,
            userContribution = UserContribution.MINOR,
            recoverability = Recoverability.MEDIUM,
            allowSexualReplyHelp = allowReplyHelp,
            enforcedReplyTone = enforcedTone,
            requiresDeepAnalysis = true
        )
    }
}
