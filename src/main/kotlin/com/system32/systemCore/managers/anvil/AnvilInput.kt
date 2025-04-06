package com.system32.systemCore.managers.anvil

import com.system32.systemCore.SystemCore
import com.system32.systemCore.managers.language.Language
import com.system32.systemCore.utils.text.TextUtil.Companion.color
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack


class AnvilInput (
    val player: Player,
    private val onComplete: (input: String) -> Unit,
    private val onCancel: () -> Unit
) : InventoryHolder {

    private var inventory: AnvilInventory = Bukkit.createInventory(null, InventoryType.ANVIL, color(Language.ANVIL_TITLE())) as AnvilInventory

    fun open() {
        val item = ItemStack(Material.PAPER)
        item.editMeta {
            it.displayName(color("&f"))
        }
        inventory.setItem(0, item)
        player.openInventory(inventory)
        SystemCore.anvilInputManager.register(this)

    }

    fun handleClick(event: InventoryClickEvent): Boolean {
        if (event.whoClicked != player || (event.inventory.holder !is AnvilInput)) return false

        if (event.rawSlot == 2) {
            val resultItem = inventory.getItem(2)
            val name = resultItem?.itemMeta?.displayName

            if (!name.isNullOrBlank()) {
                event.isCancelled = true
                player.closeInventory()
                SystemCore.anvilInputManager.unregister(player)
                onComplete(name)
                return true
            }
        }

        return false
    }

    fun handleClose(event: InventoryCloseEvent): Boolean {
        if (event.player != player) return false
        SystemCore.anvilInputManager.unregister(player)
        onCancel()
        return true
    }

    override fun getInventory(): Inventory {
        return inventory
    }


}