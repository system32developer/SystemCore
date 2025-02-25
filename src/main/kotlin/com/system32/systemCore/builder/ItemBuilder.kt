package com.system32.systemCore.builder

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.system32.systemCore.utils.ChatUtil.Companion.color
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*


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

    fun displayName(displayName: String): ItemBuilder {
        meta.displayName(color(displayName))
        return this
    }

    fun lore(vararg lore: String): ItemBuilder {
        meta.lore(color(lore.toList()))
        return this
    }

    fun build(): ItemStack {
        itemStack.itemMeta = meta
        return itemStack
    }
}