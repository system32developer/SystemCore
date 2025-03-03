package com.system32.systemCore.gui.guis

import com.system32.systemCore.gui.components.GuiAction
import com.system32.systemCore.gui.components.util.ItemNbt
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.inventory.ItemStack

class GuiListener : Listener {
    /**
     * Handles what happens when a player clicks on the GUI
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler
    fun onGuiClick(event: InventoryClickEvent) {
        if (event.inventory.holder !is BaseGui) return

        // Gui
        val gui = event.inventory.holder as BaseGui?

        // Executes the outside click action
        val outsideClickAction: GuiAction<InventoryClickEvent>? = gui!!.getOutsideClickAction()
        if (outsideClickAction != null && event.clickedInventory == null) {
            outsideClickAction.execute(event)
            return
        }

        if (event.clickedInventory == null) return

        // Default click action and checks weather or not there is a default action and executes it
        val defaultTopClick: GuiAction<InventoryClickEvent>? = gui.getDefaultTopClickAction()
        if (defaultTopClick != null && event.clickedInventory!!.type != InventoryType.PLAYER) {
            defaultTopClick.execute(event)
        }

        // Default click action and checks weather or not there is a default action and executes it
        val playerInventoryClick: GuiAction<InventoryClickEvent>? = gui.getPlayerInventoryAction()
        if (playerInventoryClick != null && event.clickedInventory!!.type == InventoryType.PLAYER) {
            playerInventoryClick.execute(event)
        }

        // Default click action and checks weather or not there is a default action and executes it
        val defaultClick: GuiAction<InventoryClickEvent>? = gui.getDefaultClickAction()
        defaultClick?.execute(event)

        // Slot action and checks weather or not there is a slot action and executes it
        val slotAction: GuiAction<InventoryClickEvent>? = gui.getSlotAction(event.slot)
        if (slotAction != null && event.clickedInventory!!.type != InventoryType.PLAYER) {
            slotAction.execute(event)
        }

        var guiItem: GuiItem?

        // Checks whether it's a paginated gui or not
        if (gui is PaginatedGui) {
            val paginatedGui: PaginatedGui = gui

            // Gets the gui item from the added items or the page items
            guiItem = paginatedGui.getGuiItem(event.getSlot())
            if (guiItem == null) guiItem = paginatedGui.getPageItem(event.getSlot())
        } else {
            // The clicked GUI Item
            guiItem = gui.getGuiItem(event.getSlot())
        }

        if (!isGuiItem(event.getCurrentItem(), guiItem)) return

        // Executes the action of the item
        val itemAction: GuiAction<InventoryClickEvent>? = guiItem!!.getAction()
        if (itemAction != null) itemAction.execute(event)
    }

    /**
     * Handles what happens when a player clicks on the GUI
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler
    fun onGuiDrag(event: InventoryDragEvent) {
        if (event.inventory.holder !is BaseGui) return

        // Gui
        val gui = event.inventory.holder as BaseGui?

        // Default click action and checks weather or not there is a default action and executes it
        val dragAction: GuiAction<InventoryDragEvent>? = gui!!.getDragAction()
        dragAction?.execute(event)
    }

    /**
     * Handles what happens when the GUI is closed
     *
     * @param event The InventoryCloseEvent
     */
    @EventHandler
    fun onGuiClose(event: InventoryCloseEvent) {
        if (event.inventory.holder !is BaseGui) return

        // GUI
        val gui = event.inventory.holder as BaseGui?

        // The GUI action for closing
        val closeAction: GuiAction<InventoryCloseEvent>? = gui!!.getCloseGuiAction()

        // Checks if there is or not an action set and executes it
        if (closeAction != null && !gui.isUpdating && gui.shouldRunCloseAction()) closeAction.execute(event)
    }

    /**
     * Handles what happens when the GUI is opened
     *
     * @param event The InventoryOpenEvent
     */
    @EventHandler
    fun onGuiOpen(event: InventoryOpenEvent) {
        if (event.inventory.holder !is BaseGui) return

        // GUI
        val gui = event.inventory.holder as BaseGui?

        // The GUI action for opening
        val openAction: GuiAction<InventoryOpenEvent>? = gui!!.getOpenGuiAction()

        // Checks if there is or not an action set and executes it
        if (openAction != null && !gui.isUpdating) openAction.execute(event)
    }

    /**
     * Checks if the item is or not a GUI item
     *
     * @param currentItem The current item clicked
     * @param guiItem     The GUI item in the slot
     * @return Whether it is or not a GUI item
     */
    private fun isGuiItem(currentItem: ItemStack?, guiItem: GuiItem?): Boolean {
        if (currentItem == null || guiItem == null) return false
        // Checks whether the Item is truly a GUI Item
        val nbt: String? = ItemNbt.getString(currentItem, "mf-gui")
        if (nbt == null) return false
        return nbt == guiItem.uuid.toString()
    }
}