package com.system32.systemCore.gui.components.nbt

import org.bukkit.inventory.ItemStack


interface NbtWrapper {
    fun setString(itemStack: ItemStack, key: String?, value: String?): ItemStack?

    fun removeTag(itemStack: ItemStack, key: String?): ItemStack?

    fun setBoolean(itemStack: ItemStack, key: String?, value: Boolean): ItemStack?

    fun getString(itemStack: ItemStack, key: String?): String?
}
