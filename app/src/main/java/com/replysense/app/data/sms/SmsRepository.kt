package com.replysense.app.data.sms

import android.content.ContentResolver
import android.provider.Telephony

class SmsRepository(
    private val contentResolver: ContentResolver
) {

    fun getThreadMessages(threadId: Long): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            ),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val address = it.getString(0) ?: ""
                val body = it.getString(1) ?: ""
                val date = it.getLong(2)
                val type = it.getInt(3)

                messages.add(
                    SmsMessage(
                        address = address,
                        body = body,
                        timestamp = date,
                        isFromUser = type == Telephony.Sms.MESSAGE_TYPE_SENT
                    )
                )
            }
        }

        return messages
    }
}
