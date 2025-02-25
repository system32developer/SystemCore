package com.system32.systemCore

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class TestEvent : Listener {
    init {
        SystemCore.getInstance().server.pluginManager.registerEvents(this, SystemCore.getInstance())
    }
    @EventHandler
    fun onPlayerJoin(playerJoinEvent: PlayerJoinEvent) {
        println("Player joined")
    }
}