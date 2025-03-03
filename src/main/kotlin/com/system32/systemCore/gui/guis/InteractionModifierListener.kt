package com.system32.systemCore.gui.guis

import com.google.common.base.Preconditions
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import java.util.*


/**
 * Listener that apply default GUI [InteractionModifier][dev.triumphteam.gui.components.InteractionModifier]s to all GUIs
 *
 * @author SecretX
 * @since 3.0.0
 */
class InteractionModifierListener : Listener {
    /**
     * Handles any click on GUIs, applying all [InteractionModifier][dev.triumphteam.gui.components.InteractionModifier] as required
     *
     * @param event The InventoryClickEvent
     * @author SecretX
     * @since 3.0.0
     */
    @EventHandler
    fun onGuiClick(event: InventoryClickEvent) {
        if (event.getInventory().getHolder() !is BaseGui) return

        // Gui
        val gui = event.getInventory().getHolder() as BaseGui?

        if (gui!!.allInteractionsDisabled()) {
            event.setCancelled(true)
            event.setResult(Event.Result.DENY)
            return
        }

        // if player is trying to do a disabled action, cancel it
        if ((!gui.canPlaceItems() && isPlaceItemEvent(event)) || (!gui.canTakeItems() && isTakeItemEvent(event)) || (!gui.canSwapItems() && isSwapItemEvent(
                event
            )) || (!gui.canDropItems() && isDropItemEvent(event)) || (!gui.allowsOtherActions() && isOtherEvent(
                event
            ))
        ) {
            event.setCancelled(true)
            event.setResult(Event.Result.DENY)
        }
    }

    /**
     * Handles any item drag on GUIs, applying all [InteractionModifier][dev.triumphteam.gui.components.InteractionModifier] as required
     *
     * @param event The InventoryDragEvent
     * @author SecretX
     * @since 3.0.0
     */
    @EventHandler
    fun onGuiDrag(event: InventoryDragEvent) {
        if (event.getInventory().getHolder() !is BaseGui) return

        // Gui
        val gui = event.getInventory().getHolder() as BaseGui?

        if (gui!!.allInteractionsDisabled()) {
            event.setCancelled(true)
            event.setResult(Event.Result.DENY)
            return
        }

        // if players are allowed to place items on the GUI, or player is not dragging on GUI, return
        if (gui.canPlaceItems() || !isDraggingOnGui(event)) return

        // cancel the interaction
        event.setCancelled(true)
        event.setResult(Event.Result.DENY)
    }

    /**
     * Checks if what is happening on the [InventoryClickEvent] is take an item from the GUI
     *
     * @param event The InventoryClickEvent
     * @return True if the [InventoryClickEvent] is for taking an item from the GUI
     * @author SecretX
     * @since 3.0.0
     */
    private fun isTakeItemEvent(event: InventoryClickEvent?): Boolean {
        Preconditions.checkNotNull<InventoryClickEvent?>(event, "event cannot be null")

        val inventory = event!!.getInventory()
        val clickedInventory = event.getClickedInventory()
        val action = event.getAction()

        // magic logic, simplified version of https://paste.helpch.at/tizivomeco.cpp
        if (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER || inventory.getType() == InventoryType.PLAYER) {
            return false
        }

        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY || isTakeAction(action)
    }

    /**
     * Checks if what is happening on the [InventoryClickEvent] is place an item on the GUI
     *
     * @param event The InventoryClickEvent
     * @return True if the [InventoryClickEvent] is for placing an item from the GUI
     * @author SecretX
     * @since 3.0.0
     */
    private fun isPlaceItemEvent(event: InventoryClickEvent?): Boolean {
        Preconditions.checkNotNull<InventoryClickEvent?>(event, "event cannot be null")

        val inventory = event!!.getInventory()
        val clickedInventory = event.getClickedInventory()
        val action = event.getAction()

        // shift click on item in player inventory
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER && inventory.getType() != clickedInventory.getType()) {
            return true
        }

        // normal click on gui empty slot with item on cursor
        return isPlaceAction(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER
    }

    /**
     * Checks if what is happening on the [InventoryClickEvent] is swap any item with an item from the GUI
     *
     * @param event The InventoryClickEvent
     * @return True if the [InventoryClickEvent] is for swapping any item with an item from the GUI
     * @author SecretX
     * @since 3.0.0
     */
    private fun isSwapItemEvent(event: InventoryClickEvent?): Boolean {
        Preconditions.checkNotNull<InventoryClickEvent?>(event, "event cannot be null")

        val inventory = event!!.getInventory()
        val clickedInventory = event.getClickedInventory()
        val action = event.getAction()

        return isSwapAction(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER
    }

    private fun isDropItemEvent(event: InventoryClickEvent?): Boolean {
        Preconditions.checkNotNull<InventoryClickEvent?>(event, "event cannot be null")

        val inventory = event!!.getInventory()
        val clickedInventory = event.getClickedInventory()
        val action = event.getAction()

        return isDropAction(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER)
    }

    private fun isOtherEvent(event: InventoryClickEvent?): Boolean {
        Preconditions.checkNotNull<InventoryClickEvent?>(event, "event cannot be null")

        val inventory = event!!.getInventory()
        val clickedInventory = event.getClickedInventory()
        val action = event.getAction()

        return isOtherAction(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER)
    }

    /**
     * Checks if any item is being dragged on the GUI
     *
     * @param event The InventoryDragEvent
     * @return True if the [InventoryDragEvent] is for dragging an item inside the GUI
     * @author SecretX
     * @since 3.0.0
     */
    private fun isDraggingOnGui(event: InventoryDragEvent?): Boolean {
        Preconditions.checkNotNull<InventoryDragEvent?>(event, "event cannot be null")
        val topSlots = event!!.getView().getTopInventory().getSize()
        // is dragging on any top inventory slot
        return event.getRawSlots().stream().anyMatch { slot: Int? -> slot!! < topSlots }
    }

    private fun isTakeAction(action: InventoryAction?): Boolean {
        Preconditions.checkNotNull<InventoryAction?>(action, "action cannot be null")
        return ITEM_TAKE_ACTIONS.contains(action)
    }

    private fun isPlaceAction(action: InventoryAction?): Boolean {
        Preconditions.checkNotNull<InventoryAction?>(action, "action cannot be null")
        return ITEM_PLACE_ACTIONS.contains(action)
    }

    private fun isSwapAction(action: InventoryAction?): Boolean {
        Preconditions.checkNotNull<InventoryAction?>(action, "action cannot be null")
        return ITEM_SWAP_ACTIONS.contains(action)
    }

    private fun isDropAction(action: InventoryAction?): Boolean {
        Preconditions.checkNotNull<InventoryAction?>(action, "action cannot be null")
        return ITEM_DROP_ACTIONS.contains(action)
    }

    private fun isOtherAction(action: InventoryAction?): Boolean {
        Preconditions.checkNotNull<InventoryAction?>(action, "action cannot be null")
        return action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN
    }

    companion object {
        /**
         * Holds all the actions that should be considered "take" actions
         */
        private val ITEM_TAKE_ACTIONS: MutableSet<InventoryAction?> = Collections.unmodifiableSet<InventoryAction?>(
            EnumSet.of<InventoryAction?>(
                InventoryAction.PICKUP_ONE,
                InventoryAction.PICKUP_SOME,
                InventoryAction.PICKUP_HALF,
                InventoryAction.PICKUP_ALL,
                InventoryAction.COLLECT_TO_CURSOR,
                InventoryAction.HOTBAR_SWAP,
                InventoryAction.MOVE_TO_OTHER_INVENTORY
            )
        )

        /**
         * Holds all the actions that should be considered "place" actions
         */
        private val ITEM_PLACE_ACTIONS: MutableSet<InventoryAction?> = Collections.unmodifiableSet<InventoryAction?>(
            EnumSet.of<InventoryAction?>(
                InventoryAction.PLACE_ONE,
                InventoryAction.PLACE_SOME,
                InventoryAction.PLACE_ALL
            )
        )

        /**
         * Holds all actions relating to swapping items
         */
        private val ITEM_SWAP_ACTIONS: MutableSet<InventoryAction?> = Collections.unmodifiableSet<InventoryAction?>(
            EnumSet.of<InventoryAction?>(
                InventoryAction.HOTBAR_SWAP,
                InventoryAction.SWAP_WITH_CURSOR,
                InventoryAction.HOTBAR_MOVE_AND_READD
            )
        )

        /**
         * Holds all actions relating to dropping items
         */
        private val ITEM_DROP_ACTIONS: MutableSet<InventoryAction?> = Collections.unmodifiableSet<InventoryAction?>(
            EnumSet.of<InventoryAction?>(
                InventoryAction.DROP_ONE_SLOT,
                InventoryAction.DROP_ALL_SLOT,
                InventoryAction.DROP_ONE_CURSOR,
                InventoryAction.DROP_ALL_CURSOR
            )
        )
    }
}
