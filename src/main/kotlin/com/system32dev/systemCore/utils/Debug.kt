package com.system32dev.systemCore.utils

import com.system32dev.systemCore.SystemCore
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Debug(
    private val baseFolder: File = SystemCore.plugin.dataFolder,
    var printToConsole: Boolean = true,
    var minLevel: Level = Level.INFO,
    var maxLinesPerFile: Int = 5_000
) {

    enum class Level { DEBUG, INFO, WARN, ERROR }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val logsRoot = File(baseFolder, "logs")
    private var writer: PrintWriter
    private var currentFile: File
    private var linesWritten = 0
    private var sessionIndex: Int

    init {
        val today = LocalDate.now().format(dateFormatter)
        val dayFolder = File(logsRoot, today).also { it.mkdirs() }
        sessionIndex = nextSessionIndex(dayFolder, today)
        val fileName = "$today-$sessionIndex.txt"
        currentFile = File(dayFolder, fileName)
        writer = PrintWriter(FileWriter(currentFile, true))
        write(Level.INFO, "=== Session $sessionIndex started ===")
    }

    fun write(message: String) = write(Level.INFO, message)

    fun debug(message: String) = write(Level.DEBUG, message)

    fun warn(message: String) = write(Level.WARN, message)

    fun error(message: String, throwable: Throwable? = null) {
        write(Level.ERROR, message)
        throwable?.let {
            val sw = java.io.StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString().lines().forEach { line -> write(Level.ERROR, "  $line") }
        }
    }

    fun separator(title: String = "") {
        val bar = if (title.isEmpty()) "─".repeat(60)
        else "─── $title " + "─".repeat(maxOf(0, 56 - title.length))
        write(Level.INFO, bar)
    }

    fun close() {
        write(Level.INFO, "=== Session $sessionIndex closed ===")
        writer.flush()
        writer.close()
    }

    private fun write(level: Level, message: String) {
        if (level.ordinal < minLevel.ordinal) return
        val line = "[${LocalDateTime.now().format(timeFormatter)}] [${level.name.padEnd(5)}] $message"
        if (printToConsole) println(line)
        writer.println(line)
        writer.flush()
        if (++linesWritten >= maxLinesPerFile) rotate()
    }

    private fun rotate() {
        writer.flush()
        writer.close()
        val today = LocalDate.now().format(dateFormatter)
        val dayFolder = File(logsRoot, today).also { it.mkdirs() }
        sessionIndex++
        currentFile = File(dayFolder, "$today-$sessionIndex.txt")
        writer = PrintWriter(FileWriter(currentFile, true))
        linesWritten = 0
    }

    private fun nextSessionIndex(dayFolder: File, today: String): Int {
        val existing = dayFolder.listFiles { f -> f.name.matches(Regex("$today-\\d+\\.txt")) }
        return (existing?.size ?: 0) + 1
    }
}