package com.system32dev.systemCore.managers

import com.system32dev.systemCore.SystemCore
import com.system32dev.systemCore.processor.annotations.Service
import com.system32dev.systemCore.processor.model.PluginService
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID


object MovementManager: Listener {

    init {
        SystemCore.event(this)
    }

    private val cache = mutableListOf<UUID>()

    fun disallow(player: Player) {
        if (!cache.contains(player.uniqueId)) {
            cache.add(player.uniqueId)
        }
    }

    fun allow(player: Player) {
        cache.remove(player.uniqueId)
    }

    fun isDisallowed(player: Player): Boolean {
        return cache.contains(player.uniqueId)
    }

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        val player = e.player
        if (!cache.contains(player.uniqueId)) return
        if (!isOnGround(player)) return
        val to: Location = e.from
        to.pitch = e.to.pitch
        to.yaw = e.to.yaw
        e.setTo(to)
    }

    fun isOnGround(player: Player): Boolean {
        val box = player.boundingBox
        val below = box.shift(0.0, -0.05, 0.0)
        return player.wouldCollideUsing(below)
    }
}