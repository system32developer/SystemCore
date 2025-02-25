package com.system32.systemCore

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class SystemCore {
    companion object {
        private val plugin: Plugin by lazy {
            JavaPlugin.getProvidingPlugin(TestEvent::class.java)
        }

        fun getInstance(): Plugin {
            return plugin
        }
    }
}
