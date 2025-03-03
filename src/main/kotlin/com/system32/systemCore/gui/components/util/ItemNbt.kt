package com.system32.systemCore.gui.components.util

import com.system32.systemCore.gui.components.nbt.LegacyNbt
import com.system32.systemCore.gui.components.nbt.NbtWrapper
import com.system32.systemCore.gui.components.nbt.Pdc
import org.bukkit.inventory.ItemStack

object ItemNbt {
    private val nbt: NbtWrapper = selectNbt()

    fun setString(itemStack: ItemStack, key: String, value: String): ItemStack {
        return nbt.setString(itemStack, key, value)!!
    }

    fun getString(itemStack: ItemStack, key: String): String {
        return nbt.getString(itemStack, key)!!
    }

    fun setBoolean(itemStack: ItemStack, key: String, value: Boolean): ItemStack {
        return nbt.setBoolean(itemStack, key, value)!!
    }


    fun removeTag(itemStack: ItemStack, key: String): ItemStack {
        return nbt.removeTag(itemStack, key)!!
    }


    private fun selectNbt(): NbtWrapper {
        if (VersionHelper.IS_PDC_VERSION) return Pdc()
        return LegacyNbt()
    }
}