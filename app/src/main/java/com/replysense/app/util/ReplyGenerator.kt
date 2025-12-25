package com.replysense.app.util

object ReplyGenerator {

    /**
     * Pick the “best” message to reply to: usually the last substantive message.
     * (If OCR includes name headers, we still just pick the last long-ish message.)
     */
    fun pickBestMessageIndex(messages: List<OcrPostProcess.Msg>): Int {
        // Prefer last message that looks substantive
        for (i in messages.indices.reversed()) {
            val t = messages[i].text.trim()
            if (t.length >= 12) return i
        }
        return messages.lastIndex.coerceAtLeast(0)
    }

    fun generate(tone: String, variant: Any, incoming: String, transcriptContext: String): String {
        val v = when (variant) {
            is Enum<*> -> variant.name.lowercase()
            else -> variant.toString().lowercase()
        }
        return generate(tone, v, incoming, transcriptContext)
    }

    fun generate(tone: String, variant: String, incoming: String, transcriptContext: String): String {
        val t = tone.lowercase()
        val msg = incoming.trim()
        if (msg.isBlank()) return "Send me the message you want to reply to."

        val base = when (t) {
            "chill" -> chill(msg)
            "flirty" -> flirty(msg)
            "firm" -> firm(msg)
            "savage" -> savage(msg)
            else -> chill(msg)
        }

        return rewriteVariant(base, variant)
    }

    fun buildPrompt(tone: String, variant: String, selectedMessage: String, transcriptContext: String): String {
        return """
You are ReplySense. Write ONE text message reply.
Tone: $tone
Rewrite style: $variant (default|shorter|kinder|direct)
Rules:
- 1–2 sentences max (shorter = 1 sentence if possible)
- Match vibe; not cringe
- No mention of OCR or AI
Context transcript:
$transcriptContext

Message to reply to:
$selectedMessage

Return ONLY the reply text.
""".trim()
    }

    private fun rewriteVariant(base: String, variant: String): String {
        return when (variant) {
            "shorter" -> shorten(base)
            "kinder" -> kinder(base)
            "direct" -> direct(base)
            else -> base
        }
    }

    private fun shorten(s: String): String {
        // Brutal trim: keep first sentence or first ~90 chars.
        val firstSentence = s.split(Regex("""(?<=[.!?])\s+""")).firstOrNull().orEmpty()
        val t = if (firstSentence.isNotBlank()) firstSentence else s
        return if (t.length <= 90) t else t.take(88).trimEnd() + "…"
    }

    private fun kinder(s: String): String {
        // Add softness without emojis
        val t = s.trim()
        return when {
            t.startsWith("Cool story.") -> "Okay, fair — " + t.removePrefix("Cool story.").trim()
            t.contains("Drop the exact") -> t.replace("Drop the exact", "Can you share the exact")
            else -> "Totally — " + t.replaceFirstChar { it.uppercase() }
        }
    }

    private fun direct(s: String): String {
        // Remove fluff words
        return s.replace(Regex("""\b(Okay|Alright|Totally|Yeah)\b[, ]*"""), "").trim().ifBlank { s }
    }

    private fun chill(msg: String): String {
        val isQuestion = msg.contains("?")
        val mentionsPlans = msg.contains("weekend", true) || msg.contains("tonight", true) || msg.contains("tomorrow", true) || msg.contains("this week", true)
        val mentionsSleep = msg.contains("sleep", true) || msg.contains("bed", true)
        val mentionsSanta = msg.contains("santa", true)

        return when {
            mentionsSanta -> "😂 Alright, I’m going to sleep. Don’t want Santa to blacklist me."
            mentionsSleep -> "Fair. I’m crashing — I’ll hit you tomorrow."
            mentionsPlans -> "That works. What day/time are you thinking?"
            isQuestion -> "Yeah, I’m down. What did you have in mind?"
            else -> "Got you. I’ll let you know in a bit."
        }
    }

    private fun flirty(msg: String): String {
        val isQuestion = msg.contains("?")
        val mentionsPlans = msg.contains("weekend", true) || msg.contains("tonight", true) || msg.contains("tomorrow", true) || msg.contains("this week", true)
        val mentionsSleep = msg.contains("sleep", true) || msg.contains("bed", true)
        val mentionsSanta = msg.contains("santa", true)

        return when {
            mentionsSanta -> "Okay okay 😄 I’m going to bed… but you better text me tomorrow."
            mentionsSleep -> "You’re right. I’m going to sleep… unless you’re trying to keep me up 😌"
            mentionsPlans -> "Week sounds better — pick the day and I’ll make it happen."
            isQuestion -> "Maybe 😏 convince me."
            else -> "Mmm noted. I like where this is going."
        }
    }

    private fun firm(msg: String): String {
        val isQuestion = msg.contains("?")
        val mentionsPlans = msg.contains("weekend", true) || msg.contains("tonight", true) || msg.contains("tomorrow", true) || msg.contains("this week", true)
        val mentionsSleep = msg.contains("sleep", true) || msg.contains("bed", true)
        val mentionsSanta = msg.contains("santa", true)

        return when {
            mentionsSanta -> "Yep. I’m going to bed. Santa can keep his record clean."
            mentionsSleep -> "You’re right — I’m going to sleep. We’ll talk tomorrow."
            mentionsPlans -> "Tell me the day/time and I’ll confirm."
            isQuestion -> "Answer me with specifics and we’ll do it."
            else -> "Okay. Keep me posted with details."
        }
    }

    private fun savage(msg: String): String {
        val isQuestion = msg.contains("?")
        val mentionsPlans = msg.contains("weekend", true) || msg.contains("tonight", true) || msg.contains("tomorrow", true) || msg.contains("this week", true)
        val mentionsSleep = msg.contains("sleep", true) || msg.contains("bed", true)
        val mentionsSanta = msg.contains("santa", true)

        return when {
            mentionsSanta -> "Fine. I’m going to sleep. Tell Santa I want a refund on adulthood."
            mentionsSleep -> "Facts. I’m going to bed — you’re not about to ruin my sleep schedule."
            mentionsPlans -> "Cool. Drop the exact day/time or it’s not happening."
            isQuestion -> "Sure. But make it make sense."
            else -> "Alright. Don’t be weird about it."
        }
    }
}
