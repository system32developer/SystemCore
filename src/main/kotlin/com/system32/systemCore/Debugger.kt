package com.system32.systemCore

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Debugger(val enabled: Boolean = false, val sessionLogging: Boolean = false) {


    private val debugFolder = File(SystemCore.plugin.dataFolder, "debug")
    private var attempt = 1
    private lateinit var logFile: File
    private lateinit var writer: PrintWriter

    init {
        setupLogger()
    }

    private fun setupLogger() {
        if (!enabled) return
        if (!debugFolder.exists()) debugFolder.mkdirs()

        attempt = calculateAttempt()

        if (sessionLogging) {
            debugFolder.listFiles()?.forEach {
                if (it.isFile && it.name == "debugger.txt") it.delete()
            }

            val dateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-HH-mm")
            val formatted = dateTime.format(formatter)

            logFile = File(debugFolder, "$attempt - $formatted.txt")
        } else {
            debugFolder.listFiles()?.forEach {
                if (it.isFile && it.name != "debugger.txt") it.delete()
            }
            logFile = File(debugFolder, "debugger.txt")
        }

        if (!logFile.exists()) logFile.createNewFile()
        writer = PrintWriter(FileWriter(logFile, true), true)

        log("=== Logger started ===")
    }

    private fun calculateAttempt(): Int {
        val logFiles = debugFolder.listFiles { file -> file.isFile && file.name.endsWith(".txt") }
        var maxAttempt = 0

        logFiles?.forEach { file ->
            val regex = Regex("""^(\d+) - """)
            val matchResult = regex.find(file.name)
            if (matchResult != null) {
                val attemptNumber = matchResult.groupValues[1].toIntOrNull()
                if (attemptNumber != null && attemptNumber > maxAttempt) {
                    maxAttempt = attemptNumber
                }
            }
        }

        return maxAttempt + 1
    }


    fun write(message: String) {
        if (!enabled) return
        val now = LocalDateTime.now()
        val hour = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val day = now.format(DateTimeFormatter.ofPattern("EEEE"))

        val logLine = "[$hour - $day - $attempt] $message"
        writer.println(logLine)
    }

    private fun log(message: String) {
        writer.println(message)
    }

    fun close() {
        log("=== Logger closed ===")
        writer.close()
    }
}
