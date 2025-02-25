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
    private val itemMeta = itemStack.itemMeta

    companion object {
        fun from(itemStack: ItemStack): ItemBuilder {
            return ItemBuilder(itemStack)
        }

        fun from(material: Material): ItemBuilder {
            return ItemBuilder(ItemStack(material))
        }
    }


    fun displayName(displayName: String): ItemBuilder {
        itemMeta.displayName(color(displayName))
        return this
    }

    fun lore(vararg lore: String): ItemBuilder {
        itemMeta.lore(color(lore.toList()))
        return this
    }

    fun head(texture: String): ItemBuilder {
        if(itemStack.type != Material.PLAYER_HEAD) return this

        itemStack.editMeta<SkullMeta>(SkullMeta::class.java) { skullMeta: SkullMeta ->
            val uuid = UUID.randomUUID()
            val playerProfile: PlayerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16))
            playerProfile.setProperty(ProfileProperty("textures", texture))
            skullMeta.playerProfile = playerProfile
        }
        return this
    }

    fun build(): ItemStack {
        itemStack.itemMeta = itemMeta
        return itemStack
    }
}