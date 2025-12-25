package com.replysense.app

import android.app.Application
import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class ReplySenseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val previous = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                pw.println("Thread: ${thread.name}")
                pw.println("Exception: ${throwable::class.java.name}")
                pw.println("Message: ${throwable.message}")
                pw.println()
                throwable.printStackTrace(pw)
                pw.flush()

                CrashStore.save(this, sw.toString())
            } catch (_: Throwable) {
                // ignore
            } finally {
                // Let Android kill us normally
                previous?.uncaughtException(thread, throwable) ?: run {
                    exitProcess(10)
                }
            }
        }
    }
}

object CrashStore {
    private const val PREF = "replysense_crash"
    private const val KEY = "last_crash"

    fun save(context: Context, crash: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, crash)
            .apply()
    }

    fun read(context: Context): String? {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, null)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY)
            .apply()
    }
}
