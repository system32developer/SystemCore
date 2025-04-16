package com.system32.systemCore.database

class InsertBuilder {
    val data = mutableMapOf<String, Any>()

    fun set(column: String, value: Any) {
        data[column] = value
    }
}
