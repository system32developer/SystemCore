package com.system32.systemCore.gui.components

import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder


class InventoryProvider {
    fun interface Chest {
        fun getInventory(
            title: Component?,
            owner: InventoryHolder,
            rows: Int
        ): Inventory
    }

    fun interface Typed {
        fun getInventory(
            title: Component?,
            owner: InventoryHolder,
            inventoryType: InventoryType
        ): Inventory
    }
}
