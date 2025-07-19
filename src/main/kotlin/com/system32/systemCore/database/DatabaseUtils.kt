package com.system32.systemCore.database

import com.system32.systemCore.SystemCore
import org.jetbrains.exposed.v1.jdbc.exists
import java.io.File

object DatabaseUtils {
    fun localDatabase(): File {
        val file = File(SystemCore.plugin.dataFolder, "database.db")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        return file
    }
    fun localDatabaseUrl(): String {
        return "jdbc:sqlite:${localDatabase().absolutePath}"
    }
}