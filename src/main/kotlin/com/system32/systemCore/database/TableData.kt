package com.system32.systemCore.database

/**
 * Represents the data types supported for table columns.
 * Each type will map to its corresponding SQLite type.
 */
enum class TableData {

    /**
     * Short string (up to 255 characters).
     *
     * SQLite: VARCHAR(255)
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.STRING, "username", TableFlag.PRIMARY_KEY)
     * set("username", "john_doe")
     * ```
     */
    STRING,

    /**
     * Long text data.
     *
     * SQLite: TEXT
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.TEXT, "description")
     * set("description", "This is a long description.")
     * ```
     */
    TEXT,

    /**
     * 32-bit Integer number.
     *
     * SQLite: INTEGER
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.INT, "age")
     * set("age", 30)
     * ```
     */
    INT,

    /**
     * 64-bit Long integer number.
     *
     * SQLite: BIGINT
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.LONG, "user_id", TableFlag.UNIQUE)
     * set("user_id", 1234567890123456789L)
     * ```
     */
    LONG,

    /**
     * Floating-point number (single precision).
     *
     * SQLite: FLOAT
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.FLOAT, "height")
     * ```
     */
    FLOAT,

    /**
     * Floating-point number (double precision).
     *
     * SQLite: DOUBLE
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.DOUBLE, "balance")
     * set("balance", 12345.67)
     * ```
     */
    DOUBLE,

    /**
     * Boolean value.
     *
     * SQLite: BOOLEAN (stored as 0 or 1)
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.BOOLEAN, "isActive")
     * set("isActive", true)
     * ```
     */
    BOOLEAN,

    /**
     * Binary Large Object.
     *
     * SQLite: BLOB
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.BLOB, "profile_picture")
     * set("profile_picture", byteArrayOf(0x01, 0x02, 0x03))
     * ```
     */
    BLOB,

    /**
     * Date value (no time).
     * Stored as ISO format text: YYYY-MM-DD.
     *
     * SQLite: DATE
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.DATE, "birth_date")
     * set("birth_date", "1990-01-01")
     * ```
     */
    DATE,

    /**
     * Date and time value.
     * Stored as ISO format text: YYYY-MM-DD HH:MM:SS.
     *
     * SQLite: DATETIME
     *
     * Example:
     * ```kotlin
     * table.addData(TableData.DATETIME, "created_at")
     * set("created_at", "2023-10-01 12:00:00")
     * ```
     */
    DATETIME
}
