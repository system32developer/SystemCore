package com.system32.systemCore.database

import java.sql.SQLException

class Table(val name: String, val database: Database) {
    private val columns = mutableListOf<Column>()

    fun addData(type: TableData, name: String, vararg flags: TableFlag): Table {
        val column = Column(name, type, flags.toSet())
        columns.add(column)
        return this
    }

    fun getColumns(): List<Column> = columns

    fun generateCreateSQL(): String {
        val columnsSQL = columns.joinToString(", ") { col ->
            val typeSQL = when (col.type) {
                TableData.STRING -> "VARCHAR(255)"
                TableData.TEXT -> "TEXT"
                TableData.INT -> "INTEGER"
                TableData.LONG -> "BIGINT"
                TableData.FLOAT -> "FLOAT"
                TableData.DOUBLE -> "DOUBLE"
                TableData.BOOLEAN -> "BOOLEAN"
                TableData.BLOB -> "BLOB"
                TableData.DATE -> "DATE"
                TableData.DATETIME -> "DATETIME"
            }


            val flagsSQL = buildList {
                if (TableFlag.NON_NULL in col.flags) add("NOT NULL")
                if (TableFlag.PRIMARY_KEY in col.flags) add("PRIMARY KEY")
                if (TableFlag.AUTO_INCREMENT in col.flags) add("AUTOINCREMENT")
                if (TableFlag.UNIQUE in col.flags) add("UNIQUE")
            }.joinToString(" ")

            "${col.name} $typeSQL $flagsSQL".trim()
        }

        return "CREATE TABLE IF NOT EXISTS $name ($columnsSQL);"
    }

    /**
     * Insert data into the table.
     *
     * @param builder A lambda function to build the data to be inserted.
     *
     * ### Example usage:
     * ```kotlin
     * val usersTable = database.addTable("users")
     *
     * .addData(TableData.STRING, "username", TableFlag.PRIMARY_KEY, TableFlag.NON_NULL)
     * .addData(TableData.STRING, "email", TableFlag.NON_NULL)
     * .addData(TableData.INT, "age")
     *
     * database.createTable("users")
     *
     * usersTable.insert {
     *  set("username", "john_doe")
     *  set("email", "john@example.com")
     *  set("age", 30)
     * }
     *
     * ```
     */

    fun insert(builder: InsertBuilder.() -> Unit) {
        val insertData = InsertBuilder().apply(builder).data
        val columns = insertData.keys.joinToString(", ")
        val placeholders = insertData.keys.joinToString(", ") { "?" }
        val sql = "INSERT INTO $name ($columns) VALUES ($placeholders);"

        try {
            database.connection?.prepareStatement(sql).use { stmt ->
                insertData.values.forEachIndexed { index, value ->
                    stmt?.setObject(index + 1, value)
                }
                stmt?.executeUpdate()
                println("Data inserted into table '$name'")
            }
        } catch (e: SQLException) {
            println("Error inserting data into table '$name': ${e.message}")
        }
    }

    /**
     * Select data from the table.
     *
     * @param builder A lambda function to build the query.
     *
     * ### Example usage:
     * ```kotlin
     * val usersTable = database.addTable("users")
     *
     * .addData(TableData.STRING, "username", TableFlag.PRIMARY_KEY, TableFlag.NON_NULL)
     * .addData(TableData.STRING, "email", TableFlag.NON_NULL)
     * .addData(TableData.INT, "age")
     *
     * usersTable.select {
     *    columns("username, email")
     *    where("age > 18") //Or where("username = 'john_doe'")
     * }
     *
     * ```
     */

    fun select(builder: QueryBuilder.() -> Unit): List<Map<String, Any?>> {
        val queryData = QueryBuilder().apply(builder).data
        val columns = queryData["columns"] as String
        val whereClause = queryData["whereClause"] as? String
        val sql = "SELECT $columns FROM $name" + (whereClause?.let { " WHERE $it" } ?: "")

        val results = mutableListOf<Map<String, Any?>>()

        try {
            database.connection?.prepareStatement(sql).use { stmt ->
                val rs = stmt?.executeQuery()
                val meta = rs?.metaData
                val columnCount = meta?.columnCount ?: 0

                while (rs?.next() == true) {
                    val row = mutableMapOf<String, Any?>()
                    for (i in 1..columnCount) {
                        row[meta?.getColumnName(i) ?: "unknown_column"] = rs.getObject(i)
                    }
                    results.add(row)
                }
            }
        } catch (e: SQLException) {
            println("Error querying data from table '$name': ${e.message}")
        }

        return results
    }

    fun selectAll(): List<Map<String, Any?>> {
        val sql = "SELECT * FROM $name"

        val results = mutableListOf<Map<String, Any?>>()

        try {
            database.connection?.prepareStatement(sql).use { stmt ->
                val rs = stmt?.executeQuery()
                val meta = rs?.metaData
                val columnCount = meta?.columnCount ?: 0

                while (rs?.next() == true) {
                    val row = mutableMapOf<String, Any?>()
                    for (i in 1..columnCount) {
                        row[meta?.getColumnName(i) ?: "unknown_column"] = rs.getObject(i)
                    }
                    results.add(row)
                }
            }
        } catch (e: SQLException) {
            println("Error querying all data from table '$name': ${e.message}")
        }

        return results
    }

    /**
     * Checks if the table contains data that matches the given query.
     *
     * * @param builder A lambda function to build the query.
     *
     * ### Example usage:
     * ```kotlin
     * val usersTable = database.addTable("users")
     *
     * .addData(TableData.STRING, "username", TableFlag.PRIMARY_KEY, TableFlag.NON_NULL)
     * .addData(TableData.STRING, "email", TableFlag.NON_NULL)
     * .addData(TableData.INT, "age")
     *
     * database.createTable("users")
     *
     * usersTable.has {
     *    columns("username, email") //or columns("*") to select all columns
     *    where("age > 18") //Or where("username = 'john_doe'")
     * }
     *
     */

    fun has(builder: QueryBuilder.() -> Unit): Boolean {
        val queryData = QueryBuilder().apply(builder).data
        val columns = queryData["columns"] as String
        val whereClause = queryData["whereClause"] as? String
        val sql = "SELECT $columns FROM $name" + (whereClause?.let { " WHERE $it" } ?: "")

        return try {
            database.connection?.prepareStatement(sql).use { stmt ->
                val rs = stmt?.executeQuery()
                rs?.next() == true
            }
        } catch (e: SQLException) {
            println("Error checking data in table '$name': ${e.message}")
            false
        }
    }

}
