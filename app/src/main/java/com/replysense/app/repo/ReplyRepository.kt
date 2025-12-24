package com.replysense.app.repo

import com.replysense.app.net.Api
import com.replysense.app.net.ConversationTurn
import com.replysense.app.net.ReplyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReplyRepository {

    suspend fun generate(
        theirMessage: String,
        variants: Int,
        vibe: String,
        tone: String,
        writingStyle: String,
        textQuality: String,
        emojiLevel: String,
        spiceLevel: String,
        age: String,
        punctuationPreference: String,
    ) = withContext(Dispatchers.IO) {

        val req = ReplyRequest(
            conversation = listOf(
                ConversationTurn(from = "them", text = theirMessage)
            ),
            variants = variants,
            vibe = vibe,
            tone = tone,
            writingStyle = writingStyle,
            textQuality = textQuality,
            emojiLevel = emojiLevel,
            spiceLevel = spiceLevel,
            age = age,
            punctuationPreference = punctuationPreference,
        )

        Api.generateReplies(req)
    }
}
