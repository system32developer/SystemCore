package com.system32.systemCore.gui.components

import org.bukkit.event.inventory.InventoryType


enum class GuiType(val inventoryType: InventoryType, val limit: Int, val fillSize: Int) {
    CHEST(InventoryType.CHEST, 9, 9),
    WORKBENCH(InventoryType.WORKBENCH, 9, 10),
    HOPPER(InventoryType.HOPPER, 5, 5),
    DISPENSER(InventoryType.DISPENSER, 8, 9),
    BREWING(InventoryType.BREWING, 4, 5)
}
