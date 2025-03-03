package com.system32.systemCore.gui.guis

import com.system32.systemCore.SystemCore
import com.system32.systemCore.gui.components.GuiAction
import com.system32.systemCore.gui.components.InteractionModifier
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import com.system32.systemCore.gui.components.GuiContainer
import com.system32.systemCore.gui.components.GuiType
import com.system32.systemCore.gui.components.exception.GuiException
import com.system32.systemCore.gui.components.util.GuiFiller
import com.system32.systemCore.gui.components.util.VersionHelper
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.Contract
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.function.Consumer
import kotlin.Int
import kotlin.collections.MutableMap


/**
 * Base class that every GUI extends.
 * Contains all the basics for the GUI to work.
 * Main and simplest implementation of this is [Gui].
 */
@Suppress("unused")
abstract class BaseGui(guiContainer: GuiContainer, interactionModifiers: MutableSet<InteractionModifier>) :
    InventoryHolder {
    /**
     * Gets the [GuiFiller] that it's used for filling up the GUI in specific ways.
     *
     * @return The [GuiFiller].
     */
    // Gui filler.
    val filler: GuiFiller = GuiFiller(this)

    // Contains all items the GUI will have.
    private val guiItems: MutableMap<Int, GuiItem>

    // Actions for specific slots.
    private val slotActions: MutableMap<Int, GuiAction<InventoryClickEvent>>

    // Interaction modifiers.
    private val interactionModifiers: MutableSet<InteractionModifier>

    // GUI control
    private val guiContainer: GuiContainer

    // Main inventory.
    private var inventory: Inventory

    // Action to execute when clicking on any item.
    private var defaultClickAction: GuiAction<InventoryClickEvent>? = null

    // Action to execute when clicking on the top part of the GUI only.
    private var defaultTopClickAction: GuiAction<InventoryClickEvent>? = null

    // Action to execute when clicking on the player Inventory.
    private var playerInventoryAction: GuiAction<InventoryClickEvent>? = null

    // Action to execute when dragging the item on the GUI.
    private var dragAction: GuiAction<InventoryDragEvent>? = null

    // Action to execute when GUI closes.
    private var closeGuiAction: GuiAction<InventoryCloseEvent>? = null

    // Action to execute when GUI opens.
    private var openGuiAction: GuiAction<InventoryOpenEvent>? = null

    // Action to execute when clicked outside the GUI.
    private var outsideClickAction: GuiAction<InventoryClickEvent>? = null

    /**
     * Checks whether or not the GUI is updating.
     *
     * @return Whether the GUI is updating or not.
     */
    /**
     * Sets the updating status of the GUI.
     *
     * @param this.isUpdating Sets the GUI to the updating status.
     */
    // Whether the GUI is updating.
    var isUpdating: Boolean = false

    // Whether should run the actions from the close and open methods.
    private var runCloseAction = true
    private val runOpenAction = true

    init {
        this.interactionModifiers = safeCopyOf(interactionModifiers)
        this.guiContainer = guiContainer
        this.inventory = guiContainer.createInventory(this)
        this.slotActions = LinkedHashMap<Int, GuiAction<InventoryClickEvent>>()
        this.guiItems = LinkedHashMap<Int, GuiItem>()
    }

    /**
     * Copy a set into an EnumSet, required because [EnumSet.copyOf] throws an exception if the collection passed as argument is empty.
     *
     * @param set The set to be copied.
     * @return An EnumSet with the provided elements from the original set.
     */
    private fun safeCopyOf(set: MutableSet<InteractionModifier>): MutableSet<InteractionModifier> {
        return if (set.isEmpty()) EnumSet.noneOf(InteractionModifier::class.java)
        else EnumSet.copyOf(set)
    }

    /**
     * Gets the GUI title as a [Component].
     *
     * @return The GUI title [Component].
     */
    fun title(): Component {
        return guiContainer.title()
    }

    /**
     * Sets the [GuiItem] to a specific slot on the GUI.
     *
     * @param slot    The GUI slot.
     * @param guiItem The [GuiItem] to add to the slot.
     */
    fun setItem(slot: Int, guiItem: GuiItem) {
        validateSlot(slot)
        guiItems.put(slot, guiItem)
    }

    /**
     * Removes the given [GuiItem] from the GUI.
     *
     * @param item The item to remove.
     */
    fun removeItem(item: GuiItem) {
        guiItems.entries
            .stream()
            .filter { it: MutableMap.MutableEntry<Int, GuiItem> -> it.value == item }
            .findFirst()
            .ifPresent(Consumer { it: MutableMap.MutableEntry<Int, GuiItem> ->
                guiItems.remove(it.key)
                inventory.remove(it.value.itemStack)
            })
    }

    /**
     * Removes the given [ItemStack] from the GUI.
     *
     * @param item The item to remove.
     */
    fun removeItem(item: ItemStack) {
        guiItems.entries
            .stream()
            .filter { it: MutableMap.MutableEntry<Int, GuiItem> -> it.value.itemStack == item }
            .findFirst()
            .ifPresent(Consumer { it: MutableMap.MutableEntry<Int, GuiItem> ->
                guiItems.remove(it.key)
                inventory.remove(item)
            })
    }

    /**
     * Removes the [GuiItem] in the specific slot.
     *
     * @param slot The GUI slot.
     */
    fun removeItem(slot: Int) {
        validateSlot(slot)
        guiItems.remove(slot)
        inventory.setItem(slot, null)
    }

    /**
     * Alternative [.removeItem] with cols and rows.
     *
     * @param row The row.
     * @param col The column.
     */
    fun removeItem(row: Int, col: Int) {
        removeItem(getSlotFromRowCol(row, col))
    }

    /**
     * Alternative [.setItem] to set item that takes a [List] of slots instead.
     *
     * @param slots   The slots in which the item should go.
     * @param guiItem The [GuiItem] to add to the slots.
     */
    fun setItem(slots: MutableList<Int?>, guiItem: GuiItem) {
        for (slot in slots) {
            setItem(slot!!, guiItem)
        }
    }

    /**
     * Alternative [.setItem] to set item that uses *ROWS* and *COLUMNS* instead of slots.
     *
     * @param row     The GUI row number.
     * @param col     The GUI column number.
     * @param guiItem The [GuiItem] to add to the slot.
     */
    fun setItem(row: Int, col: Int, guiItem: GuiItem) {
        setItem(getSlotFromRowCol(row, col), guiItem)
    }

    /**
     * Adds [GuiItem]s to the GUI without specific slot.
     * It'll set the item to the next empty slot available.
     *
     * @param items Varargs for specifying the [GuiItem]s.
     */
    open fun addItem(vararg items: GuiItem) {
        this.addItem(false, *items)
    }

    /**
     * Adds [GuiItem]s to the GUI without specific slot.
     * It'll set the item to the next empty slot available.
     *
     * @param items        Varargs for specifying the [GuiItem]s.
     * @param expandIfFull If true, expands the gui if it is full
     * and there are more items to be added
     */
    fun addItem(expandIfFull: Boolean, vararg items: GuiItem) {
        val notAddedItems: MutableList<GuiItem> = ArrayList<GuiItem>()
        val rows: Int = guiContainer.rows()
        val guiType: GuiType = guiContainer.guiType()

        for (guiItem in items) {
            for (slot in 0..<rows * 9) {
                if (guiItems[slot] != null) {
                    if (slot == rows * 9 - 1) {
                        notAddedItems.add(guiItem)
                    }
                    continue
                }

                guiItems.put(slot, guiItem)
                break
            }
        }

        if (!expandIfFull || rows >= 6 || notAddedItems.isEmpty() || guiType !== GuiType.CHEST) {
            return
        }

        if (guiContainer !is GuiContainer.Chest) return
        guiContainer.rows(guiContainer.rows() + 1)
        this.inventory = guiContainer.createInventory(this)
        this.update()
        this.addItem(true, *notAddedItems.toTypedArray<GuiItem>())
    }

    /**
     * Adds a [GuiAction] for when clicking on a specific slot.
     * See [InventoryClickEvent].
     *
     * @param slot       The slot that will trigger the [GuiAction].
     * @param slotAction [GuiAction] to resolve when clicking on specific slots.
     */
    fun addSlotAction(slot: Int, slotAction: GuiAction<InventoryClickEvent>) {
        validateSlot(slot)
        slotActions.put(slot, slotAction)
    }

    /**
     * Alternative method for [.addSlotAction] to add a [GuiAction] to a specific slot using *ROWS* and *COLUMNS* instead of slots.
     * See [InventoryClickEvent].
     *
     * @param row        The row of the slot.
     * @param col        The column of the slot.
     * @param slotAction [GuiAction] to resolve when clicking on the slot.
     */
    fun addSlotAction(row: Int, col: Int, slotAction: GuiAction<InventoryClickEvent>) {
        addSlotAction(getSlotFromRowCol(row, col), slotAction)
    }

    /**
     * Gets a specific [GuiItem] on the slot.
     *
     * @param slot The slot of the item.
     * @return The [GuiItem] on the introduced slot or `null` if doesn't exist.
     */
    fun getGuiItem(slot: Int): GuiItem? {
        return guiItems[slot]
    }

    /**
     * Opens the GUI for a [HumanEntity].
     *
     * @param player The [HumanEntity] to open the GUI to.
     */
    open fun open(player: HumanEntity) {
        if (player.isSleeping) return

        inventory.clear()
        populateGui()
        player.openInventory(inventory)
    }

    /**
     * Closes the GUI with a `2 tick` delay (to prevent items from being taken from the [Inventory]).
     *
     * @param player         The [HumanEntity] to close the GUI to.
     * @param runCloseAction If should or not run the close action.
     */
    /**
     * Closes the GUI with a `2 tick` delay (to prevent items from being taken from the [Inventory]).
     *
     * @param player The [HumanEntity] to close the GUI to.
     */
    @JvmOverloads
    fun close(player: HumanEntity, runCloseAction: Boolean = true) {
        val task = Runnable {
            this.runCloseAction = runCloseAction
            player.closeInventory()
            this.runCloseAction = true
        }

        if (VersionHelper.IS_FOLIA) {
            if (GET_SCHEDULER_METHOD == null || EXECUTE_METHOD == null) {
                throw GuiException("Could not find Folia Scheduler methods.")
            }

            try {
                EXECUTE_METHOD!!.invoke(GET_SCHEDULER_METHOD!!.invoke(player), plugin, task, null, 2L)
            } catch (e: IllegalAccessException) {
                throw GuiException("Could not invoke Folia task.", e)
            } catch (e: InvocationTargetException) {
                throw GuiException("Could not invoke Folia task.", e)
            }
            return
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, 2L)
    }

    /**
     * Updates the GUI for all the [Inventory] views.
     */
    open fun update() {
        inventory.clear()
        populateGui()
        // for (HumanEntity viewer : new ArrayList<>(inventory.getViewers())) ((Player) viewer).updateInventory();
    }

    /**
     * Updates the title of the GUI.
     * *This method may cause LAG if used on a loop*.
     *
     * @param title The title to set.
     * @return The GUI for easier use when declaring, works like a builder.
     */
    @Contract("_ -> this")
    open fun updateTitle(title: Component): BaseGui {
        this.isUpdating = true

        val viewers: MutableList<HumanEntity> = ArrayList<HumanEntity>(inventory.viewers)

        guiContainer.title(title) // Update the title.
        inventory = guiContainer.createInventory(this)

        for (player in viewers) {
            open(player)
        }

        this.isUpdating = false
        return this
    }

    /**
     * Updates the specified item in the GUI at runtime, without creating a new [GuiItem].
     *
     * @param slot      The slot of the item to update.
     * @param itemStack The [ItemStack] to replace in the original one in the [GuiItem].
     */
    fun updateItem(slot: Int, itemStack: ItemStack) {
        val guiItem: GuiItem? = guiItems[slot]

        if (guiItem == null) {
            updateItem(slot, GuiItem(itemStack))
            return
        }

        guiItem.itemStack = itemStack
        updateItem(slot, guiItem)
    }

    /**
     * Alternative [.updateItem] that takes *ROWS* and *COLUMNS* instead of slots.
     *
     * @param row       The row of the slot.
     * @param col       The columns of the slot.
     * @param itemStack The [ItemStack] to replace in the original one in the [GuiItem].
     */
    fun updateItem(row: Int, col: Int, itemStack: ItemStack) {
        updateItem(getSlotFromRowCol(row, col), itemStack)
    }

    /**
     * Alternative [.updateItem] but creates a new [GuiItem].
     *
     * @param slot The slot of the item to update.
     * @param item The [GuiItem] to replace in the original.
     */
    fun updateItem(slot: Int, item: GuiItem) {
        guiItems.put(slot, item)
        inventory.setItem(slot, item.itemStack)
    }

    /**
     * Alternative [.updateItem] that takes *ROWS* and *COLUMNS* instead of slots.
     *
     * @param row  The row of the slot.
     * @param col  The columns of the slot.
     * @param item The [GuiItem] to replace in the original.
     */
    fun updateItem(row: Int, col: Int, item: GuiItem) {
        updateItem(getSlotFromRowCol(row, col), item)
    }

    /**
     * Disable item placement inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @Contract(" -> this")
    fun disableItemPlace(): BaseGui {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_PLACE)
        return this
    }

    /**
     * Disable item retrieval inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @Contract(" -> this")
    fun disableItemTake(): BaseGui {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_TAKE)
        return this
    }

    /**
     * Disable item swap inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @Contract(" -> this")
    fun disableItemSwap(): BaseGui {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_SWAP)
        return this
    }

    /**
     * Disable item drop inside the GUI
     *
     * @return The BaseGui
     * @since 3.0.3.
     */
    @Contract(" -> this")
    fun disableItemDrop(): BaseGui {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_DROP)
        return this
    }

    /**
     * Disable other GUI actions
     * This option pretty much disables creating a clone stack of the item
     *
     * @return The BaseGui
     * @since 3.0.4
     */
    @Contract(" -> this")
    fun disableOtherActions(): BaseGui {
        interactionModifiers.add(InteractionModifier.PREVENT_OTHER_ACTIONS)
        return this
    }

    /**
     * Disable all the modifications of the GUI, making it immutable by player interaction.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @Contract(" -> this")
    fun disableAllInteractions(): BaseGui {
        interactionModifiers.addAll(InteractionModifier.VALUES)
        return this
    }

    /**
     * Allows item placement inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @Contract(" -> this")
    fun enableItemPlace(): BaseGui {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_PLACE)
        return this
    }

    /**
     * Allow items to be taken from the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @Contract(" -> this")
    fun enableItemTake(): BaseGui {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_TAKE)
        return this
    }

    /**
     * Allows item swap inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @Contract(" -> this")
    fun enableItemSwap(): BaseGui {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_SWAP)
        return this
    }

    /**
     * Allows item drop inside the GUI
     *
     * @return The BaseGui
     * @since 3.0.3
     */
    @Contract(" -> this")
    fun enableItemDrop(): BaseGui {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_DROP)
        return this
    }

    /**
     * Enable other GUI actions
     * This option pretty much enables creating a clone stack of the item
     *
     * @return The BaseGui
     * @since 3.0.4
     */
    @Contract(" -> this")
    fun enableOtherActions(): BaseGui {
        interactionModifiers.remove(InteractionModifier.PREVENT_OTHER_ACTIONS)
        return this
    }

    /**
     * Enable all modifications of the GUI, making it completely mutable by player interaction.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @Contract(" -> this")
    fun enableAllInteractions(): BaseGui {
        interactionModifiers.clear()
        return this
    }

    fun allInteractionsDisabled(): Boolean {
        return interactionModifiers.size == InteractionModifier.VALUES.size
    }

    /**
     * Check if item placement is allowed inside this GUI.
     *
     * @return True if item placement is allowed for this GUI.
     * @author SecretX.
     * @since 3.0.0.
     */
    fun canPlaceItems(): Boolean {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_PLACE)
    }

    /**
     * Check if item retrieval is allowed inside this GUI.
     *
     * @return True if item retrieval is allowed inside this GUI.
     * @author SecretX.
     * @since 3.0.0.
     */
    fun canTakeItems(): Boolean {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_TAKE)
    }

    /**
     * Check if item swap is allowed inside this GUI.
     *
     * @return True if item swap is allowed for this GUI.
     * @author SecretX.
     * @since 3.0.0.
     */
    fun canSwapItems(): Boolean {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_SWAP)
    }

    /**
     * Check if item drop is allowed inside this GUI
     *
     * @return True if item drop is allowed for this GUI
     * @since 3.0.3
     */
    fun canDropItems(): Boolean {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_DROP)
    }

    /**
     * Check if any other actions are allowed in this GUI
     *
     * @return True if other actions are allowed
     * @since 3.0.4
     */
    fun allowsOtherActions(): Boolean {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_OTHER_ACTIONS)
    }

    /**
     * Gets an immutable [Map] with all the GUI items.
     *
     * @return The [Map] with all the [.guiItems].
     */
    fun getGuiItems(): MutableMap<Int, GuiItem> {
        return guiItems
    }

    /**
     * Gets the main [Inventory] of this GUI.
     *
     * @return Gets the [Inventory] from the holder.
     */
    override fun getInventory(): Inventory {
        return inventory
    }

    /**
     * Sets the new inventory of the GUI.
     *
     * @param inventory The new inventory.
     */
    fun setInventory(inventory: Inventory) {
        this.inventory = inventory
    }

    val rows: Int
        /**
         * Gets the amount of [.guiContainer] rows.
         *
         * @return The [.guiContainer]'s rows of the GUI.
         */
        get() = guiContainer.rows()

    /**
     * Gets the [GuiType] in use.
     *
     * @return The [GuiType].
     */
    fun guiType(): GuiType {
        return guiContainer.guiType()
    }

    /**
     * Gets the default click resolver.
     */
    fun getDefaultClickAction(): GuiAction<InventoryClickEvent>? {
        return defaultClickAction
    }

    /**
     * Sets the [GuiAction] of a default click on any item.
     * See [InventoryClickEvent].
     *
     * @param defaultClickAction [GuiAction] to resolve when any item is clicked.
     */
    fun setDefaultClickAction(defaultClickAction: GuiAction<InventoryClickEvent>?) {
        this.defaultClickAction = defaultClickAction
    }

    /**
     * Gets the default top click resolver.
     */
    fun getDefaultTopClickAction(): GuiAction<InventoryClickEvent>? {
        return defaultTopClickAction
    }

    /**
     * Sets the [GuiAction] of a default click on any item on the top part of the GUI.
     * Top inventory being for example chests etc, instead of the [Player] inventory.
     * See [InventoryClickEvent].
     *
     * @param defaultTopClickAction [GuiAction] to resolve when clicking on the top inventory.
     */
    fun setDefaultTopClickAction(defaultTopClickAction: GuiAction<InventoryClickEvent>?) {
        this.defaultTopClickAction = defaultTopClickAction
    }

    /**
     * Gets the player inventory action.
     */
    fun getPlayerInventoryAction(): GuiAction<InventoryClickEvent>? {
        return playerInventoryAction
    }

    fun setPlayerInventoryAction(playerInventoryAction: GuiAction<InventoryClickEvent>?) {
        this.playerInventoryAction = playerInventoryAction
    }

    /**
     * Gets the default drag resolver.
     */
    fun getDragAction(): GuiAction<InventoryDragEvent>? {
        return dragAction
    }

    /**
     * Sets the [GuiAction] of a default drag action.
     * See [InventoryDragEvent].
     *
     * @param dragAction [GuiAction] to resolve.
     */
    fun setDragAction(dragAction: GuiAction<InventoryDragEvent>?) {
        this.dragAction = dragAction
    }

    /**
     * Gets the close gui resolver.
     */
    fun getCloseGuiAction(): GuiAction<InventoryCloseEvent>? {
        return closeGuiAction
    }

    /**
     * Sets the [GuiAction] to run once the inventory is closed.
     * See [InventoryCloseEvent].
     *
     * @param closeGuiAction [GuiAction] to resolve when the inventory is closed.
     */
    fun setCloseGuiAction(closeGuiAction: GuiAction<InventoryCloseEvent>?) {
        this.closeGuiAction = closeGuiAction
    }

    /**
     * Gets the open gui resolver.
     */
    fun getOpenGuiAction(): GuiAction<InventoryOpenEvent>? {
        return openGuiAction
    }

    /**
     * Sets the [GuiAction] to run when the GUI opens.
     * See [InventoryOpenEvent].
     *
     * @param openGuiAction [GuiAction] to resolve when opening the inventory.
     */
    fun setOpenGuiAction(openGuiAction: GuiAction<InventoryOpenEvent>?) {
        this.openGuiAction = openGuiAction
    }

    /**
     * Gets the resolver for the outside click.
     */
    fun getOutsideClickAction(): GuiAction<InventoryClickEvent>? {
        return outsideClickAction
    }

    /**
     * Sets the [GuiAction] to run when clicking on the outside of the inventory.
     * See [InventoryClickEvent].
     *
     * @param outsideClickAction [GuiAction] to resolve when clicking outside of the inventory.
     */
    fun setOutsideClickAction(outsideClickAction: GuiAction<InventoryClickEvent>?) {
        this.outsideClickAction = outsideClickAction
    }

    /**
     * Gets the action for the specified slot.
     *
     * @param slot The slot clicked.
     */
    fun getSlotAction(slot: Int): GuiAction<InventoryClickEvent>? {
        return slotActions[slot]
    }

    /**
     * Populates the GUI with it's items.
     */
    fun populateGui() {
        for (entry in guiItems.entries) {
            inventory.setItem(entry.key, entry.value.itemStack)
        }
    }

    fun shouldRunCloseAction(): Boolean {
        return runCloseAction
    }

    fun shouldRunOpenAction(): Boolean {
        return runOpenAction
    }

    /**
     * Gets the slot from the row and column passed.
     *
     * @param row The row.
     * @param col The column.
     * @return The slot needed.
     */
    fun getSlotFromRowCol(row: Int, col: Int): Int {
        return (col + (row - 1) * 9) - 1
    }

    /**
     * Checks if the slot introduces is a valid slot.
     *
     * @param slot The slot to check.
     */
    private fun validateSlot(slot: Int) {
        val guiType: GuiType = guiContainer.guiType()
        val limit: Int = guiType.limit

        if (guiType === GuiType.CHEST) {
            if (slot < 0 || slot >= guiContainer.rows() * limit) throwInvalidSlot(slot)
            return
        }

        if (slot < 0 || slot > limit) throwInvalidSlot(slot)
    }

    /**
     * Throws an exception if the slot is invalid.
     *
     * @param slot The specific slot to display in the error message.
     */
    private fun throwInvalidSlot(slot: Int) {
        if (guiContainer.guiType() === GuiType.CHEST) {
            throw GuiException(
                "Slot " + slot + " is not valid for the gui type - " + guiContainer.guiType()
                    .name + " and rows - " + guiContainer.rows() + "!"
            )
        }

        throw GuiException("Slot " + slot + " is not valid for the gui type - " + guiContainer.guiType().name + "!")
    }

    protected fun guiContainer(): GuiContainer {
        return guiContainer
    }

    companion object {
        // The plugin instance for registering the event and for the close delay.
        private val plugin: Plugin = SystemCore.getInstance()

        private var GET_SCHEDULER_METHOD: Method? = null
        private var EXECUTE_METHOD: Method? = null

        // Registering the listener class.
        init {
            try {
                GET_SCHEDULER_METHOD = Entity::class.java.getMethod("getScheduler")
                val entityScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler")
                EXECUTE_METHOD = entityScheduler.getMethod(
                    "execute",
                    Plugin::class.java,
                    Runnable::class.java,
                    Runnable::class.java,
                    Long::class.javaPrimitiveType
                )
            } catch (ignored: NoSuchMethodException) {
            } catch (ignored: ClassNotFoundException) {
            }

            Bukkit.getPluginManager().registerEvents(GuiListener(), plugin)
            Bukkit.getPluginManager().registerEvents(InteractionModifierListener(), plugin)
        }
    }
}
