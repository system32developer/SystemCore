package com.system32.systemCore.database.table

data class Column(
    val name: String,
    val type: TableData,
    val flags: Set<TableFlag>
)