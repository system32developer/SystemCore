package com.system32.systemCore.database.builders

data class ForeignKey(
    val column: String,
    val referenceTable: String,
    val referenceColumn: String
)
