package com.system32.systemCore.builder

import com.system32.systemCore.utils.ChatUtil.Companion.color
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack


class ItemBuilder(private val itemStack: ItemStack) {
    private val meta = itemStack.itemMeta

    companion object {
        fun from(itemStack: ItemStack): ItemBuilder {
            return ItemBuilder(itemStack)
        }

        fun from(material: Material): ItemBuilder {
            return ItemBuilder(ItemStack(material))
        }
    }

    fun name(displayName: String): ItemBuilder {
        meta.displayName(color(displayName))
        return this
    }

    fun lore(vararg lore: String): ItemBuilder {
        meta.lore(color(lore.toList()))
        return this
    }

    fun lore(lore: List<String>): ItemBuilder {
        meta.lore(color(lore))
        return this
    }

    fun build(): ItemStack {
        itemStack.itemMeta = meta
        return itemStack
    }

}