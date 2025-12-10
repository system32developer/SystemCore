package com.system32dev.systemCore.generated

import com.system32dev.systemCore.processor.model.PluginService
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import com.system32dev.systemCore.processor.model.SystemRegistry
import com.system32dev.systemCore.processor.annotations.AutoRegistry

@AutoRegistry
object ServiceRegistry : SystemRegistry {
    val services: List<PluginService> = listOf(
        {{services}}
    )

    override fun onLoad(plugin: JavaPlugin) {
        services.forEach { it.onLoad() }
    }

    override fun onEnable(plugin: JavaPlugin) {
        services.forEach { it.onEnable() }
        registerListeners(plugin)
    }

    override fun onDisable(plugin: JavaPlugin) {
        services.forEach { it.onDisable() }
    }

    fun reload() {
        services.forEach { it.onReload() }
    }

    fun registerListeners(plugin: JavaPlugin){
        services.forEach { obj ->
            if (obj !is Listener) {
                plugin.logger.warning("[EventRegistry] ${obj::class.java.name} is not implementing Listener interface.")
                return@forEach
            }
            plugin.server.pluginManager.registerEvents(obj, plugin)
        }
    }
}
