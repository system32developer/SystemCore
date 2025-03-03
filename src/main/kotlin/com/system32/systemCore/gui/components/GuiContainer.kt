package com.system32.systemCore.gui.components

import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder


interface GuiContainer {
    fun title(): Component

    fun title(title: Component)

    fun createInventory(inventoryHolder: InventoryHolder): Inventory

    fun guiType(): GuiType

    fun inventorySize(): Int

    fun rows(): Int

    class Chest(
        title: Component,
        inventoryProvider: InventoryProvider.Chest,
        rows: Int
    ) : GuiContainer {
        private val inventoryProvider: InventoryProvider.Chest

        private var rows: Int
        private var title: Component?

        init {
            this.inventoryProvider = inventoryProvider
            this.title = title
            this.rows = rows
        }

        override fun title(): Component {
            return title!!
        }

        override fun title(title: Component) {
            this.title = title
        }

        override fun inventorySize(): Int {
            return rows * 9
        }

        override fun guiType(): GuiType {
            return GuiType.CHEST
        }

        override fun rows(): Int {
            return rows
        }

        fun rows(rows: Int) {
            this.rows = rows
        }

        override fun createInventory(inventoryHolder: InventoryHolder): Inventory {
            return inventoryProvider.getInventory(title, inventoryHolder, inventorySize())
        }
    }

    class Typed(
        title: Component,
        inventoryProvider: InventoryProvider.Typed,
        guiType: GuiType
    ) : GuiContainer {
        private val inventoryProvider: InventoryProvider.Typed
        private val guiType: GuiType
        private var title: Component?

        init {
            this.inventoryProvider = inventoryProvider
            this.title = title
            this.guiType = guiType
        }

        override fun title(): Component {
            return title!!
        }

        override fun title(title: Component) {
            this.title = title
        }

        override fun inventorySize(): Int {
            return guiType.limit
        }

        override fun guiType(): GuiType {
            return guiType
        }

        override fun rows(): Int {
            return 1
        }

        override fun createInventory(inventoryHolder: InventoryHolder): Inventory {
            return inventoryProvider.getInventory(title, inventoryHolder, guiType.inventoryType)
        }
    }
}
