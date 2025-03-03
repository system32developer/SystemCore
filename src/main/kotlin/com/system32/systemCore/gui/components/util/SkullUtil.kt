package com.system32.systemCore.gui.components.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*


object SkullUtil {
    private val SKULL = skullMaterial
    private val GSON = Gson()

    private val skullMaterial: Material
        get() {
            if (VersionHelper.IS_ITEM_LEGACY) {
                return Material.valueOf("SKULL_ITEM")
            }

            return Material.PLAYER_HEAD
        }


    @Suppress("deprecation")
    fun skull(): ItemStack {
        return if (VersionHelper.IS_ITEM_LEGACY) ItemStack(SKULL, 1, 3.toShort()) else ItemStack(SKULL)
    }

    @Suppress("deprecation")
    fun isPlayerSkull(item: ItemStack): Boolean {
        if (VersionHelper.IS_ITEM_LEGACY) {
            return item.type == SKULL && item.durability == 3.toShort()
        }

        return item.type == SKULL
    }

    fun getSkinUrl(base64Texture: String?): String? {
        val decoded = String(Base64.getDecoder().decode(base64Texture))
        val `object` = GSON.fromJson<JsonObject?>(decoded, JsonObject::class.java)

        val textures = `object`.get("textures")

        if (textures == null) {
            return null
        }

        val skin = textures.getAsJsonObject().get("SKIN")

        if (skin == null) {
            return null
        }

        val url = skin.getAsJsonObject().get("url")
        return url?.asString
    }
}
