package com.system32.systemCore

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Debugger() {

    var sessionLogging = false
    private val debugFolder = File(SystemCore.plugin.dataFolder, "debug")
    private var attempt = 1
    private lateinit var logFile: File
    private lateinit var writer: PrintWriter

    init {
        setupLogger()
    }

    private fun setupLogger() {
        if (!debugFolder.exists()) debugFolder.mkdirs()

        attempt = calculateAttempt()

        if (sessionLogging) {
            debugFolder.listFiles()?.forEach {
                if (it.isFile && it.name != "debugger.txt") it.delete()
            }

            val dateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH-mm ss-EEE")
            val formatted = dateTime.format(formatter)

            logFile = File(debugFolder, "$attempt - $formatted.txt")
        } else {
            logFile = File(debugFolder, "debugger.txt")
        }

        if (!logFile.exists()) logFile.createNewFile()
        writer = PrintWriter(FileWriter(logFile, true), true)

        log("=== Logger started ===")
    }

    private fun calculateAttempt(): Int {
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val pattern = Regex("""^(\d+) - .*\.txt$""")

        val attemptsToday = debugFolder.listFiles()
            ?.mapNotNull { file ->
                val match = pattern.find(file.name)
                if (match != null) {
                    val created = file.lastModified()
                    val fileDate = LocalDateTime.ofEpochSecond(created / 1000, 0, java.time.ZoneOffset.UTC)
                    if (fileDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) == date) {
                        match.groupValues[1].toIntOrNull()
                    } else null
                } else null
            } ?: emptyList()

        return if (attemptsToday.isEmpty()) 1 else (attemptsToday.maxOrNull()!! + 1)
    }

    fun write(message: String) {
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
