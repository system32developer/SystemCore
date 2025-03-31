package com.system32.systemCore

import com.system32.systemCore.managers.chat.ChatAwaiterManager
import com.system32.systemCore.managers.chat.ChatTriggerManager
import com.system32.systemCore.managers.cooldown.CooldownManager
import org.bukkit.command.CommandExecutor
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

        var githubUser = "System32"
        var githubRepo = "SystemCore"

        var githubSupport: Boolean = false
            private set
        get() = !(githubUser == "System32" && githubRepo== "SystemCore")

        /**
         * Manages cooldowns for various actions.
         */
        val cooldownManager = CooldownManager()

        /**
         * Manages chat awaiters and triggers.
         */
        val chatAwaiterManager = ChatAwaiterManager()

        /**
         * Manages chat triggers for specific keywords.
         */
        val chatTriggerManager = ChatTriggerManager()

        /**
         * Provides the main plugin instance.
         */
        val plugin: Plugin by lazy {
            JavaPlugin.getProvidingPlugin(SystemCore::class.java).also {
                event(chatTriggerManager)
                event(chatAwaiterManager)
            }
        }

        /**
         * Registers an event listener to the plugin's event system.
         *
         * @param event The event listener to register.
         */
        fun event(event: Listener){
            plugin.server.pluginManager.registerEvents(event, plugin)
        }

        /**
         * Registers a command executor to the plugin's command system.
         *
         * @param command The command executor to register.
         */

        fun command(commandName: String,command: CommandExecutor){
            (plugin as JavaPlugin).getCommand(commandName)?.setExecutor(command)
        }

    }
}