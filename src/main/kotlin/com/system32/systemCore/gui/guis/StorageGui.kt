package com.system32.systemCore.gui.guis

import com.system32.systemCore.gui.components.GuiContainer
import com.system32.systemCore.gui.components.InteractionModifier
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import org.checkerframework.checker.units.qual.K
import org.jetbrains.annotations.NotNull
import java.util.Collections


class StorageGui(
    guiContainer: GuiContainer,
    interactionModifiers: MutableSet<InteractionModifier>
) : BaseGui(guiContainer, interactionModifiers) {

    /**
     * Adds [ItemStack] to the inventory straight, not the GUI
     *
     * @param items Varargs with [ItemStack]s
     * @return An immutable [Map] with the left overs
     */
    fun addItem(vararg items: ItemStack): Map<Int, ItemStack> {
        return getInventory().addItem(*items).toMap()
    }

    /**
     * Adds [ItemStack] to the inventory straight, not the GUI
     *
     * @param items List of [ItemStack]s
     * @return An immutable [Map] with the left overs
     */
    fun addItem(items: List<ItemStack>): Map<Int, ItemStack> {
        return addItem(*items.toTypedArray())
    }

    /**
     * Overridden [BaseGui.open] to prevent opening while sleeping
     *
     * @param player The [HumanEntity] to open the GUI to
     */
    override fun open(player: HumanEntity) {
        if (player.isSleeping) return
        populateGui()
        player.openInventory(inventory)
    }
}