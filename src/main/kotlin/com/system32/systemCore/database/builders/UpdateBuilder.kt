package com.system32.systemCore.database.builders

class UpdateBuilder {
    val data = mutableMapOf<String, Any?>()
    var whereCondition: String? = null
    val rawData = mutableMapOf<String, String>()

    fun set(column: String, value: Any?) {
        data[column] = value
    }

    fun setRaw(column: String, expression: String) {
        rawData[column] = expression
    }

    fun where(condition: String) {
        whereCondition = condition
    }
}
