package com.system32.systemCore.gui.guis


import com.google.common.base.Preconditions
import com.system32.systemCore.gui.components.GuiAction
import com.system32.systemCore.gui.components.util.ItemNbt
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * GuiItem represents the [ItemStack] on the [Inventory]
 */
@Suppress("unused")
class GuiItem @JvmOverloads constructor(itemStack: ItemStack, action: GuiAction<InventoryClickEvent>? = null) {
    /**
     * Gets the random [UUID] that was generated when the GuiItem was made
     */
    // Random UUID to identify the item when clicking
    val uuid: UUID = UUID.randomUUID()

    // Action to do when clicking on the item
    private var action: GuiAction<InventoryClickEvent>?

    /**
     * Gets the GuiItem's [ItemStack]
     *
     * @return The [ItemStack]
     */
    // The ItemStack of the GuiItem
    var itemStack: ItemStack


        init {
            Preconditions.checkNotNull<ItemStack?>(itemStack, "The ItemStack for the GUI Item cannot be null!")
            if (itemStack.type != Material.AIR) {
                this.itemStack = ItemNbt.setString(itemStack.clone(), "mf-gui", uuid.toString())
            } else {
                this.itemStack = itemStack.clone()
            }
        }

    /**
     * Main constructor of the GuiItem
     *
     * @param itemStack The [ItemStack] to be used
     * @param action    The [GuiAction] to run when clicking on the Item
     */
    /**
     * Secondary constructor with no action
     *
     * @param itemStack The ItemStack to be used
     */
    init {
        Preconditions.checkNotNull<ItemStack?>(itemStack, "The ItemStack for the GUI Item cannot be null!")

        this.action = action

        // Sets the UUID to an NBT tag to be identifiable later
        this.itemStack = itemStack
    }

    /**
     * Alternate constructor that takes [Material] instead of an [ItemStack] but without a [GuiAction]
     *
     * @param material The [Material] to be used when invoking class
     */
    constructor(material: Material) : this(ItemStack(material), null)

    /**
     * Alternate constructor that takes [Material] instead of an [ItemStack]
     *
     * @param material The `Material` to be used when invoking class
     * @param action   The [GuiAction] should be passed on [InventoryClickEvent]
     */
    constructor(material: Material, action: GuiAction<InventoryClickEvent>?) : this(ItemStack(material), action)

    /**
     * Gets the [GuiAction] to do when the player clicks on it
     */
    fun getAction(): GuiAction<InventoryClickEvent>? {
        return action
    }

    /**
     * Replaces the [GuiAction] of the current GUI Item
     *
     * @param action The new [GuiAction] to set
     */
    fun setAction(action: GuiAction<InventoryClickEvent>?) {
        this.action = action
    }
}
