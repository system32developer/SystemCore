package com.system32dev.systemCore.generated

import com.system32dev.systemCore.processor.model.SystemRegistry
import org.bukkit.plugin.java.JavaPlugin

class SystemLoader(val plugin: JavaPlugin) {
    val registries: List<SystemRegistry> = listOf(
        {{registries}}
    )

    fun onLoad() {
        registries.forEach { it.onLoad(plugin) }
    }

    fun onEnable() {
        registries.forEach { it.onEnable(plugin) }
    }

    fun onDisable() {
        registries.forEach { it.onDisable(plugin) }
    }
}
