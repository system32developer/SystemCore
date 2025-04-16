package com.system32.systemCore.database

class QueryBuilder {
    val data = mutableMapOf<String, Any>()

    fun columns(columns: String) {
        data["columns"] = columns
    }

    fun where(condition: String) {
        data["whereClause"] = condition
    }
}