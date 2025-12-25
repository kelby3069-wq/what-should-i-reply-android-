package com.replysense.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtil {
    fun copy(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("ReplySense", text))
    }
}
