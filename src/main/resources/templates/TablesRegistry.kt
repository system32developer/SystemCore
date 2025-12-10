package com.system32dev.systemCore.generated

import com.system32dev.systemCore.processor.model.SystemRegistry
import com.system32dev.systemCore.processor.annotations.AutoRegistry
import org.bukkit.plugin.java.JavaPlugin
import com.system32dev.systemCore.SystemCore.isRemoteDatabase
import org.jetbrains.exposed.v1.core.Table
import com.system32dev.systemCore.database.DatabaseUtils.localDatabase

@AutoRegistry
object TablesRegistry : SystemRegistry {
    val tables: List<Table> = listOf(
        {{tables}}
    )

    override fun onLoad(plugin: JavaPlugin) {}

    override fun onEnable(plugin: JavaPlugin) {
        if(!isRemoteDatabase) localDatabase(*tables.toTypedArray())
    }

    override fun onDisable(plugin: JavaPlugin) {}
}
