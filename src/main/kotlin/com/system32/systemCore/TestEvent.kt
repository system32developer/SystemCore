package com.system32.systemCore

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class TestEvent : Listener {
    init {
        val plugin = SystemCore.getInstance()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerJoin(playerJoinEvent: PlayerJoinEvent) {
        println("Player joined")
    }
}
