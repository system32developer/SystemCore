package com.system32.systemCore.managers.anvil

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class AnvilInputManager : Listener {

    private val inputs = mutableMapOf<Player, AnvilInput>()

    fun register(input: AnvilInput) {
        inputs[input.player] = input
    }

    fun unregister(player: Player) {
        inputs.remove(player)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (event.inventory.holder !is AnvilInput) return
        val input = inputs[player] ?: return

        if (input.handleClick(event)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        inputs[player]?.handleClose(event)
    }
}