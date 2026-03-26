package com.barrettotte.fishtank.util

import android.util.Log

/** Simple logging wrapper that includes file and line number in output. */
object Logger {
    private const val PREFIX = "Fishtank"

    /** Log a debug message with file:line info. */
    fun d(tag: String, message: String) {
        val caller = Throwable().stackTrace[1]
        val location = "(${caller.fileName}:${caller.lineNumber})"
        Log.d("$PREFIX.$tag", "$location $message")
    }

    /** Log an error message with file:line info. */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val caller = Throwable().stackTrace[1]
        val location = "(${caller.fileName}:${caller.lineNumber})"

        if (throwable != null) {
            Log.e("$PREFIX.$tag", "$location $message", throwable)
        } else {
            Log.e("$PREFIX.$tag", "$location $message")
        }
    }
}
