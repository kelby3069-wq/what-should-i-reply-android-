package com.whatshouldireply.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ReplyResponse(val replies: List<String>)

object ApiClient {

    /**
     * ✅ Put your Cloudflare Worker URL here after you deploy it.
     * Example: https://what-should-i-reply.yourname.workers.dev/reply
     */
    private const val REPLY_ENDPOINT = "https://YOUR-WORKER-URL-HERE/reply"

    suspend fun generateReplies(
        fullContextPrompt: String,
        toneLabel: String,
        goal: String?
    ): ReplyResponse = withContext(Dispatchers.IO) {

        val body = JSONObject().apply {
            put("context", fullContextPrompt)
            put("tone", toneLabel)
            if (!goal.isNullOrBlank()) put("goal", goal)
        }.toString()

        val conn = (URL(REPLY_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 45_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        conn.outputStream.use { os ->
            os.write(body.toByteArray(Charsets.UTF_8))
        }

        val status = conn.responseCode
        val stream = if (status in 200..299) conn.inputStream else conn.errorStream

        val raw = BufferedReader(InputStreamReader(stream)).use { it.readText() }

        if (status !in 200..299) {
            throw IllegalStateException("AI server error ($status): ${raw.take(400)}")
        }

        // Expect: { "replies": ["...", "...", "..."] }
        val json = JSONObject(raw)
        val arr: JSONArray = json.optJSONArray("replies") ?: JSONArray()
        val replies = buildList {
            for (i in 0 until minOf(arr.length(), 6)) {
                val s = arr.optString(i).trim()
                if (s.isNotBlank()) add(s)
            }
        }

        if (replies.isEmpty()) {
            throw IllegalStateException("AI server returned no replies.")
        }

        ReplyResponse(replies)
    }
}
