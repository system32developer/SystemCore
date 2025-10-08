package com.system32dev.systemCore.managers.regions.events

import com.system32dev.systemCore.managers.regions.model.Region
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class RegionLeftEvent (
    val player: Player,
    val region: Region,
) : Event(), Cancellable {

    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}