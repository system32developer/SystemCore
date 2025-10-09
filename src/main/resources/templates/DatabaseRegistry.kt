package com.system32dev.generated

import com.system32dev.systemCore.managers.processor.PluginService
import org.jetbrains.exposed.v1.core.Table
import com.system32dev.systemCore.database.DatabaseUtils.localDatabase

object DatabaseRegistry {
    val tables: List<Table> = listOf(
        {{tables}}
    )

    init {
        localDatabase(*tables.toTypedArray())
    }
}
