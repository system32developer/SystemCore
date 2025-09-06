package com.system32.generated

import org.bukkit.event.Listener
import com.system32.systemCore.SystemCore

object EventRegistry {
    private val listeners: List<Any> = listOf(
        {{listeners}}
    )

    fun register() {
        val plugin = SystemCore.plugin
        listeners.forEach { obj ->
            if (obj !is Listener) {
                plugin.logger.warning("[EventRegistry] ${obj::class.java.name} is not implementing Listener interface.")
                return@forEach
            }
            plugin.server.pluginManager.registerEvents(obj, plugin)
        }
    }
}
