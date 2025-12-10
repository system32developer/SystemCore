package com.system32dev.systemCore

import org.bukkit.command.CommandExecutor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

object SystemCore {
    lateinit var plugin: JavaPlugin

    var isRemoteDatabase: Boolean = false

    fun event(vararg listeners: Listener) {
        listeners.forEach { plugin.server.pluginManager.registerEvents(it, plugin) }
    }

    fun legacyCommand(commandName: String, command: CommandExecutor) {
        plugin.getCommand(commandName)?.setExecutor(command)
    }
}