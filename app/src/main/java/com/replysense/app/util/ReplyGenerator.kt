package com.replysense.app.util

object ReplyGenerator {

    /**
     * Baseline: local “smart-ish” reply generator.
     * Later: replace with AI call (we already provide buildPrompt()).
     */
    fun generate(tone: String, incoming: String, transcriptContext: String): String {
        val t = tone.lowercase()
        val msg = incoming.trim()

        if (msg.isBlank()) return "Send me the message you want to reply to."

        // Quick intent guesses (cheap heuristics)
        val isQuestion = msg.endsWith("?") || msg.contains("?")
        val mentionsPlans = msg.contains("weekend", true) || msg.contains("tonight", true) || msg.contains("tomorrow", true) || msg.contains("this week", true)
        val mentionsSleep = msg.contains("sleep", true) || msg.contains("bed", true)
        val mentionsSanta = msg.contains("santa", true)

        return when (t) {
            "chill" -> chill(isQuestion, mentionsPlans, mentionsSleep, mentionsSanta, msg)
            "flirty" -> flirty(isQuestion, mentionsPlans, mentionsSleep, mentionsSanta, msg)
            "firm" -> firm(isQuestion, mentionsPlans, mentionsSleep, mentionsSanta, msg)
            "savage" -> savage(isQuestion, mentionsPlans, mentionsSleep, mentionsSanta, msg)
            else -> chill(isQuestion, mentionsPlans, mentionsSleep, mentionsSanta, msg)
        }
    }

    fun buildPrompt(tone: String, selectedMessage: String, transcriptContext: String): String {
        return """
You are ReplySense: write ONE text message reply.
Tone: $tone (keep it natural, not cringe).
Constraints:
- 1–2 sentences max.
- No emojis unless the tone calls for it.
- Match the vibe of the conversation.
- Don’t mention OCR or that you’re an AI.
Context transcript (may be noisy):
$transcriptContext

Message to reply to:
$selectedMessage

Return ONLY the reply text.
""".trim()
    }

    private fun chill(q: Boolean, plans: Boolean, sleep: Boolean, santa: Boolean, msg: String): String {
        return when {
            santa -> "😂 Alright alright, I’m going to sleep. Don’t want Santa to blacklist me."
            sleep -> "Fair. I’m crashing — I’ll hit you tomorrow."
            plans -> "That works. What day/time are you thinking?"
            q -> "Yeah, I’m down. What did you have in mind?"
            else -> "Got you. I’ll let you know in a bit."
        }
    }

    private fun flirty(q: Boolean, plans: Boolean, sleep: Boolean, santa: Boolean, msg: String): String {
        return when {
            santa -> "Okay okay 😄 I’m going to bed… but you better text me tomorrow."
            sleep -> "You’re right. I’m going to sleep… unless you’re trying to keep me up 😌"
            plans -> "Week sounds better — pick the day and I’ll make it happen."
            q -> "Maybe 😏 convince me."
            else -> "Mmm noted. I like where this is going."
        }
    }

    private fun firm(q: Boolean, plans: Boolean, sleep: Boolean, santa: Boolean, msg: String): String {
        return when {
            santa -> "Yep. I’m going to bed. Santa can keep his record clean."
            sleep -> "You’re right — I’m going to sleep. We’ll talk tomorrow."
            plans -> "Tell me the day/time and I’ll confirm. No guessing."
            q -> "Answer me with specifics and we’ll do it."
            else -> "Okay. Keep me posted with details."
        }
    }

    private fun savage(q: Boolean, plans: Boolean, sleep: Boolean, santa: Boolean, msg: String): String {
        return when {
            santa -> "Fine. I’m going to sleep. Tell Santa I want a refund on adulthood."
            sleep -> "Facts. I’m going to bed — you’re not about to ruin my sleep schedule."
            plans -> "Cool story. Drop the exact day/time or it’s not happening."
            q -> "Sure. But make it make sense."
            else -> "Alright. Don’t be weird about it."
        }
    }
}
