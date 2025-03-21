package com.system32.systemCore

import com.system32.systemCore.managers.cooldown.CooldownManager
import com.system32.systemCore.utils.DiscordUtil
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Main system core class providing utilities and core functionalities.
 * This includes cooldown management, plugin reference, and event registration.
 */
class SystemCore {

    companion object {
        /**
         * Indicates whether PlaceholderAPI support is enabled.
         */
        var placeholderAPISupport: Boolean = false
            private set
        get() = plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")

        /**
         * Manages cooldowns for various actions.
         */
        val cooldownManager = CooldownManager()

        /**
         * Provides the main plugin instance.
         */
        val plugin: Plugin by lazy {
            JavaPlugin.getProvidingPlugin(SystemCore::class.java)
        }

        /**
         * Registers an event listener to the plugin's event system.
         *
         * @param event The event listener to register.
         */
        fun register(event: Listener){
            plugin.server.pluginManager.registerEvents(event, plugin)
        }

    }
}