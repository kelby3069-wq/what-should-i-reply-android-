package com.replysense.app.data.sms

import android.content.ContentResolver
import android.provider.Telephony

class AndroidSmsReader(
    private val contentResolver: ContentResolver
) {

    fun readMessages(limit: Int = 50): List<String> {
        val messages = mutableListOf<String>()

        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY),
            null,
            null,
            "${Telephony.Sms.DATE} DESC LIMIT $limit"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val body = it.getString(0)
                if (!body.isNullOrBlank()) {
                    messages.add(body.trim())
                }
            }
        }

        return messages
    }
}
