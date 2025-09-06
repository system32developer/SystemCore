package com.system32.generated

import com.system32.systemCore.managers.processor.PluginService

object ServiceRegistry {
    val services: List<PluginService> = listOf(
        {{services}}
    )

    fun onEnable() {
        services.forEach { it.onEnable() }
    }

    fun onDisable() {
        services.forEach { it.onDisable() }
    }

    fun reload() {
        services.forEach { it.reload() }
    }
}
