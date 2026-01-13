package com.replysense.app.data.sms

data class SmsMessage(
    val address: String,
    val body: String,
    val timestamp: Long,
    val isFromUser: Boolean
)
