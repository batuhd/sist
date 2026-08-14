package com.sinop.sist.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {

    private const val TAG = "CrashLogger"
    private const val LOG_DIR = "logs"
    private const val LOG_FILE = "sist_log.txt"
    private const val MAX_LOG_BYTES = 512 * 1024

    private lateinit var logDir: File
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true
        logDir = File(context.filesDir, LOG_DIR).apply { mkdirs() }

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeLog(
                    "CRASH",
                    "Thread: ${thread.name}\n" + throwable.stackTraceToString()
                )
            } catch (_: Exception) {
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        writeLog(tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        writeLog(tag, message + (throwable?.let { "\n${it.stackTraceToString()}" } ?: ""))
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        writeLog(tag, message + (throwable?.let { "\n${it.stackTraceToString()}" } ?: ""))
    }

    fun readLogs(): String {
        if (!::logDir.isInitialized) return ""
        val crashFiles = logDir.listFiles { file -> file.name.startsWith("crash-") }
            ?.sortedByDescending { it.name }
            ?: emptyList()
        val main = logDir.resolve(LOG_FILE)

        return buildString {
            crashFiles.forEach { file ->
                appendLine("===== ${file.name} =====")
                appendLine(runCatching { file.readText() }.getOrDefault(""))
                appendLine()
            }
            if (main.exists()) {
                appendLine("===== $LOG_FILE =====")
                append(runCatching { main.readText() }.getOrDefault(""))
            }
        }.takeIf { it.isNotBlank() } ?: "Henüz kayıt yok."
    }

    fun clearLogs() {
        if (!::logDir.isInitialized) return
        logDir.listFiles()?.forEach { file ->
            runCatching { file.delete() }
        }
    }

    @Synchronized
    private fun writeLog(tag: String, message: String) {
        if (!::logDir.isInitialized) return
        runCatching {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val line = "$timestamp [$tag] $message\n"

            val targetFile = if (tag == "CRASH") {
                val crashName = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
                logDir.resolve("crash-$crashName.txt")
            } else {
                val main = logDir.resolve(LOG_FILE)
                if (main.exists() && main.length() > MAX_LOG_BYTES) {
                    main.writeText("")
                }
                main
            }

            FileWriter(targetFile, true).use { writer -> writer.append(line) }
        }
    }

    private fun Throwable.stackTraceToString(): String {
        val sw = StringWriter()
        printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}
