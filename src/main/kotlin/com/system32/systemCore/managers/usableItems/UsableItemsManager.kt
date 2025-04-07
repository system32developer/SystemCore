package com.system32.systemCore.managers.usableItems

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class UsableItemsManager : Listener {
    data class UsableItem(
        val item: ItemStack,
        val validate: (PlayerInteractEvent) -> Boolean = { true },
        val onUse: (PlayerInteractEvent) -> Unit
    )

    private val usableItems = mutableListOf<UsableItem>()

    fun register(item: ItemStack, validate: (PlayerInteractEvent) -> Boolean = { true }, onUse: (PlayerInteractEvent) -> Unit) {
        usableItems.add(UsableItem(item, validate, onUse))
    }

    @EventHandler
    fun onPlayerUse(event: PlayerInteractEvent) {
        val itemInHand = event.item ?: return
        if (event.hand != EquipmentSlot.HAND) return

        usableItems.firstOrNull { item -> item.item == itemInHand && item.validate(event) }?.onUse(event)
    }
}
