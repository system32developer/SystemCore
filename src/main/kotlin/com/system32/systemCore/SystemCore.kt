package com.system32.systemCore

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class SystemCore {
    companion object {
        private var plugin: Plugin = JavaPlugin.getProvidingPlugin(this::class.java).also { plugin = it }

        fun getInstance(): Plugin {
            return plugin
        }
    }
}
