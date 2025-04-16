package com.system32.systemCore.database

data class Column(
    val name: String,
    val type: TableData,
    val flags: Set<TableFlag>
)