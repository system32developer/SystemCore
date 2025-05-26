package com.system32.systemCore.utils.minecraft

import org.bukkit.inventory.ItemStack
import java.io.IOException
import java.util.*

object ItemUtil {
    fun toBase64(items: List<ItemStack>): String {
        return Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(items))
    }

    fun fromBase64(base64: String): List<ItemStack> {
        val bytes = Base64.getDecoder().decode(base64)
        return ItemStack.deserializeItemsFromBytes(bytes).toList()
    }
}