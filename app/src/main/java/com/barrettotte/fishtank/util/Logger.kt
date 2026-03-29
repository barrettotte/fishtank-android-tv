package com.barrettotte.fishtank.util

import android.util.Log

import com.barrettotte.fishtank.BuildConfig

/** Simple logging wrapper that only logs in debug builds. Includes file and line number in output. */
object Logger {
    private const val PREFIX = "Fishtank"

    /** Log a debug message with file:line info. No-op in release builds. */
    fun d(tag: String, message: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        val caller = Throwable().stackTrace[1]
        val location = "(${caller.fileName}:${caller.lineNumber})"
        Log.d("$PREFIX.$tag", "$location $message")
    }

    /** Log an error message with file:line info. No-op in release builds. */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) {
            return
        }
        val caller = Throwable().stackTrace[1]
        val location = "(${caller.fileName}:${caller.lineNumber})"

        if (throwable != null) {
            Log.e("$PREFIX.$tag", "$location $message", throwable)
        } else {
            Log.e("$PREFIX.$tag", "$location $message")
        }
    }
}
