package com.system32dev.generated

import com.system32dev.systemCore.managers.processor.PluginService

object ServiceRegistry {
    val services: List<PluginService> = listOf(
        {{services}}
    )

    fun onLoad() {
        services.forEach { it.onLoad() }
    }

    fun onEnable() {
        services.forEach { it.onEnable() }
    }

    fun onDisable() {
        services.forEach { it.onDisable() }
    }

    fun reload() {
        services.forEach { it.onReload() }
    }
}
