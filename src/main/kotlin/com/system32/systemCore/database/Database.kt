package com.system32.systemCore.database

import com.system32.systemCore.SystemCore
import com.system32.systemCore.database.table.Table
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.reflect.KClass

class Database {
    private val plugin = SystemCore.plugin

    var connection: Connection? = null
    private val tables = mutableMapOf<String, Table>()

    init {
        connect()
    }

    private fun connect() {
        val file = File(plugin.dataFolder, "database.db")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }

        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$file")
        createTables()
    }

    fun createTables() {
        for (table in tables.keys) {
            createTable(table)
        }
    }

    fun addTable(name: String): Table {
        val table = Table(name, this)
        tables[name] = table
        return table
    }

    private fun createTable(name: String) {
        val sql = tables[name]?.generateCreateSQL()
            ?: throw IllegalArgumentException("Table $name not found")

        connection?.createStatement()?.use { stmt ->
            stmt.execute(sql)
            println("Table '$name' created (if not exists).")
        }
    }
}