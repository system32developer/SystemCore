package com.system32.systemCore.gui.builder.gui

import com.system32.systemCore.gui.components.GuiContainer
import com.system32.systemCore.gui.components.GuiContainer.Chest
import com.system32.systemCore.gui.components.InventoryProvider
import com.system32.systemCore.gui.components.util.Legacy
import com.system32.systemCore.gui.guis.BaseGui
import org.bukkit.Bukkit
import org.jetbrains.annotations.Contract

abstract class BaseChestGuiBuilder<G : BaseGui?, B : BaseChestGuiBuilder<G, B>> : BaseGuiBuilder<G, B>() {
    /**
     * Getter for the rows
     *
     * @return The amount of rows
     */
    protected var rows: Int = 1
        private set
    private var inventoryProvider: InventoryProvider.Chest = InventoryProvider.Chest { title, owner, rows ->
        Bukkit.createInventory(
            owner,
            rows,
            Legacy.SERIALIZER.serialize(title!!)
        )
    }

    /**
     * Sets the rows for the GUI
     * This will only work on CHEST [dev.triumphteam.gui.components.GuiType]
     *
     * @param rows The number of rows
     * @return The builder
     */
    @Contract("_ -> this")
    fun rows(rows: Int): B {
        this.rows = rows
        return this as B
    }

    fun inventory(inventoryProvider: InventoryProvider.Chest): B {
        this.inventoryProvider = inventoryProvider
        return this as B
    }

    protected fun getInventoryProvider(): InventoryProvider.Chest {
        return inventoryProvider
    }

    protected fun createContainer(): GuiContainer.Chest {
        return Chest(getTitle(), inventoryProvider, this.rows)
    }
}