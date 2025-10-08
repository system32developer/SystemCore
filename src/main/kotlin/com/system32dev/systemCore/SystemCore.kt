package com.system32dev.systemCore

import com.system32dev.systemCore.managers.chat.ChatAwaiterManager
import com.system32dev.systemCore.managers.cooldown.CooldownManager
import com.system32dev.systemCore.utils.minecraft.ChatHeadUtil
import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Main system core class providing utilities and core functionalities.
 * This includes cooldown management, plugin reference, and event registration.
 */
object SystemCore {
    /**
     * Provides the main plugin instance.
     */

    lateinit var plugin: JavaPlugin

    /**
     * Registers an event listener to the plugin's event system.
     *
     * @param event The event listener to register.
     */
    fun event(vararg listeners: Listener) {
        listeners.forEach { plugin.server.pluginManager.registerEvents(it, plugin) }
    }

    /**
     * Registers a command executor to the plugin's command system.
     *
     * @param command The command executor to register.
     */

    fun legacyCommand(commandName: String, command: CommandExecutor) {
        plugin.getCommand(commandName)?.setExecutor(command)
    }
}

