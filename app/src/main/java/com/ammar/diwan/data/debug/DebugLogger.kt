package com.ammar.diwan.data.debug

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    private const val TAG = "DiwanDebug"
    private const val MAX_LOG_LINES = 200

    private val logList = mutableListOf<String>()
    private val _logsFlow = MutableStateFlow<List<String>>(emptyList())
    val logsFlow: StateFlow<List<String>> = _logsFlow.asStateFlow()

    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun init(context: Context) {
        logFile = File(context.filesDir, "diwan_debug.log")
        log("Init", "Diwan debug log recorder initialized")

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val trace = sw.toString()
            log("FATAL", "Crash on thread ${thread.name}:\n$trace")
            oldHandler?.uncaughtException(thread, throwable)
        }
    }

    @Synchronized
    fun log(tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val entry = "[$timestamp] [$tag] $message"
        Log.d(TAG, entry)

        if (logList.size >= MAX_LOG_LINES) {
            logList.removeAt(0)
        }
        logList.add(entry)
        _logsFlow.value = logList.toList()

        // Append to file asynchronously or safely
        runCatching {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    writer.write(entry + "\n")
                }
            }
        }
    }

    fun clear() {
        logList.clear()
        _logsFlow.value = emptyList()
        runCatching { logFile?.delete() }
    }
}
