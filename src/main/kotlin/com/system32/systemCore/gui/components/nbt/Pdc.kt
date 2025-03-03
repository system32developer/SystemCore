package com.system32.systemCore.gui.components.nbt

import com.system32.systemCore.SystemCore
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class Pdc : NbtWrapper {
    override fun setString(itemStack: ItemStack, key: String?, value: String?): ItemStack {
        val meta = itemStack.itemMeta
        if (meta == null) return itemStack
        meta.persistentDataContainer
            .set<String?, String?>(NamespacedKey(PLUGIN, key!!), PersistentDataType.STRING, value!!)
        itemStack.setItemMeta(meta)
        return itemStack
    }

    override fun removeTag(itemStack: ItemStack, key: String?): ItemStack {
        val meta = itemStack.itemMeta
        if (meta == null) return itemStack
        meta.persistentDataContainer.remove(NamespacedKey(PLUGIN, key!!))
        itemStack.setItemMeta(meta)
        return itemStack
    }

    override fun setBoolean(itemStack: ItemStack, key: String?, value: Boolean): ItemStack {
        val meta = itemStack.itemMeta
        if (meta == null) return itemStack
        meta.persistentDataContainer
            .set<Byte?, Byte?>(NamespacedKey(PLUGIN, key!!), PersistentDataType.BYTE, if (value) 1.toByte() else 0)
        itemStack.setItemMeta(meta)
        return itemStack
    }

    override fun getString(itemStack: ItemStack, key: String?): String? {
        val meta = itemStack.itemMeta
        if (meta == null) return null
        return meta.persistentDataContainer
            .get<String, String>(NamespacedKey(PLUGIN, key!!), PersistentDataType.STRING)
    }


    companion object {
        private val PLUGIN: Plugin = SystemCore.getInstance()
    }
}