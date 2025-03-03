package com.system32.systemCore

import com.system32.systemCore.utils.DiscordUtil
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class SystemCore {


    companion object {
        private var placeholderAPI: Boolean = false
        private val plugin: Plugin by lazy {
            JavaPlugin.getProvidingPlugin(TestEvent::class.java)
        }

        fun getInstance(): Plugin {
            return plugin
        }

        fun placeHolderAPIHook(): Boolean {
            return placeholderAPI
        }

        fun placeHolderAPIHook(boolean: Boolean) {
            placeholderAPI = boolean
        }

        fun register(event: Listener){
            plugin.server.pluginManager.registerEvents(event, plugin)
        }
    }
}
