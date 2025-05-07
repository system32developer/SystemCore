package com.system32.systemCore.database.table

import com.system32.systemCore.database.Database
import com.system32.systemCore.database.builders.InsertBuilder
import com.system32.systemCore.database.builders.QueryBuilder
import com.system32.systemCore.database.builders.UpdateBuilder
import com.system32.systemCore.utils.minecraft.ServerUtil.Companion.taskAsync
import java.sql.SQLException
import java.util.concurrent.CompletableFuture

class Table(val name: String, val database: Database) {
    private val columns = mutableListOf<Column>()

    fun addData(type: ColumnType, name: String, vararg flags: ColumnFlag): Table {
        val column = Column(name, type, flags.toSet())
        columns.add(column)
        return this
    }

    fun getColumns(): List<Column> = columns

    fun generateCreateSQL(): String {
        val columnsSQL = columns.joinToString(", ") { col ->
            val typeSQL = when (col.type) {
                ColumnType.STRING -> "VARCHAR(255)"
                ColumnType.TEXT -> "TEXT"
                ColumnType.INT -> "INTEGER"
                ColumnType.LONG -> "BIGINT"
                ColumnType.FLOAT -> "FLOAT"
                ColumnType.DOUBLE -> "DOUBLE"
                ColumnType.BOOLEAN -> "BOOLEAN"
                ColumnType.BLOB -> "BLOB"
                ColumnType.DATE -> "DATE"
                ColumnType.DATETIME -> "DATETIME"
            }


            val flagsSQL = buildList {
                if (ColumnFlag.NON_NULL in col.flags) add("NOT NULL")
                if (ColumnFlag.PRIMARY_KEY in col.flags) add("PRIMARY KEY")
                if (ColumnFlag.AUTO_INCREMENT in col.flags) add("AUTOINCREMENT")
                if (ColumnFlag.UNIQUE in col.flags) add("UNIQUE")
            }.joinToString(" ")

            "${col.name} $typeSQL $flagsSQL".trim()
        }

        return "CREATE TABLE IF NOT EXISTS $name ($columnsSQL);"
    }

    fun insertSync(builder: InsertBuilder.() -> Unit) {
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

    /**
     * Performs an asynchronous SELECT query on this table.
     *
     * This method allows you to define a normal query using the same builder pattern as `select`,
     * but the query is executed in a background thread using CompletableFuture.
     * Once the query finishes, the result is returned to the `onComplete` callback on the main thread.
     * If an exception occurs during the query, the `onError` callback will be triggered.
     *
     * @param builder A lambda to define the query conditions, similar to `select`.
     *
     * Example:
     * ```
     * table.selectAsync(
     *     {
     *         columns("*")
     *         where("uuid = '${player.uniqueId}'")
     *     },
     *     onComplete = { results ->
     *         if (results.isNotEmpty()) {
     *             val count = results.first()["count"] as? Int ?: 0
     *             player.sendMessage("You own $count spawners.")
     *         } else {
     *             player.sendMessage("You have no spawners registered.")
     *         }
     *     },
     *     onError = { error ->
     *         player.sendMessage("An error occurred while fetching your spawner data.")
     *         error.printStackTrace()
     *     }
     * )
     * ```
     *
     * @param onComplete The function to run when the query successfully completes. The result is passed as a list of rows.
     * @param onError The function to run if the query fails. Receives the thrown exception as parameter. (Optional, default prints stack trace).
     */

    fun selectAsync(
        builder: QueryBuilder.() -> Unit,
        onComplete: (List<Map<String, Any?>>) -> Unit,
        onError: (Throwable) -> Unit = { it.printStackTrace() }
    ) {
        CompletableFuture
            .supplyAsync {
                select(builder)
            }
            .whenCompleteAsync { result, throwable ->
                if (throwable != null) {
                    onError(throwable)
                } else {
                    onComplete(result)
                }
            }
    }


    fun updateSync(builder: UpdateBuilder.() -> Unit) {
        val updateData = UpdateBuilder().apply(builder)

        val setParts = mutableListOf<String>()

        updateData.data.keys.forEach { column ->
            setParts.add("$column = ?")
        }
        updateData.rawData.forEach { (column, expr) ->
            setParts.add("$column = $expr")
        }

        val setClause = setParts.joinToString(", ")
        val whereClause = updateData.whereCondition?.let { " WHERE $it" } ?: ""

        val sql = "UPDATE $name SET $setClause$whereClause;"

        try {
            database.connection?.prepareStatement(sql).use { stmt ->
                updateData.data.values.forEachIndexed { index, value ->
                    stmt?.setObject(index + 1, value)
                }
                val rowsAffected = stmt?.executeUpdate() ?: 0
            }
        } catch (e: SQLException) {
            println("Error updating data in table '$name': ${e.message}")
        }
    }

    fun deleteSync(builder: QueryBuilder.() -> Unit) {
        val queryData = QueryBuilder().apply(builder).data
        val whereClause = queryData["whereClause"] as? String
        val sql = "DELETE FROM $name" + (whereClause?.let { " WHERE $it" } ?: "")

        try {
            database.connection?.prepareStatement(sql).use { stmt ->
                val affectedRows = stmt?.executeUpdate() ?: 0
            }
        } catch (e: SQLException) {
            println("Error deleting data from table '$name': ${e.message}")
        }
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
        taskAsync { insertSync(builder) }
    }

    /**
     * Updates data in the table.
     *
     * @param builder A lambda function to build the data to be updated.
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
     * usersTable.update {
     *    set("email", "newmail@example.com")
     *    set("age", 25)
     *    where("username = 'john_doe'")
     * }
     * ```
     *
     * This will update the `email` and `age` fields in the `users` table where the `username` equals 'john_doe'.
     */

    fun update(builder: UpdateBuilder.() -> Unit) {
        taskAsync { updateSync(builder) }
    }

    /**
     * Deletes data from the table based on the given condition.
     *
     * @param builder A lambda function to build the WHERE clause of the delete query.
     *
     * ### Example usage:
     * ```kotlin
     * val usersTable = database.addTable("users")
     *     .addData(TableData.STRING, "username", TableFlag.PRIMARY_KEY, TableFlag.NON_NULL)
     *     .addData(TableData.STRING, "email", TableFlag.NON_NULL)
     *
     * usersTable.delete {
     *     where("username = 'john_doe'")
     * }
     * ```
     */

    fun delete(builder: QueryBuilder.() -> Unit) {
        taskAsync { deleteSync(builder) }
    }
}