package com.system32.systemCore.utils.minecraft

import org.bukkit.inventory.ItemStack
import java.io.IOException
import java.util.*

class ItemUtil {
    companion object {
        fun fromBase64(base64: String): ItemStack? {
            val data = Base64.getDecoder().decode(base64)
            return ItemStack.deserializeBytes(data);
        }

        fun toBase64(item: ItemStack?): String? {
            return Base64.getEncoder().encodeToString(item?.serializeAsBytes())
        }

        fun fromBase64List(base64: List<String>): List<ItemStack?> {
            return base64.map { fromBase64(it) }
        }

        fun toBase64List(items: List<ItemStack?>): List<String?> {
            return items.map { toBase64(it) }
        }
    }
}