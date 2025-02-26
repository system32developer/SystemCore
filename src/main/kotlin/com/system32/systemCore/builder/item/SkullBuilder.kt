package com.system32.systemCore.builder.item

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.system32.systemCore.builder.ItemBuilder
import com.system32.systemCore.utils.ChatUtil.Companion.color
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class SkullBuilder {
    private val itemStack: ItemStack = ItemStack(Material.PLAYER_HEAD)
    private val meta = itemStack.itemMeta as SkullMeta



    fun displayName(displayName: String): SkullBuilder {
        meta.displayName(color(displayName))
        return this
    }

    fun lore(vararg lore: String): SkullBuilder {
        meta.lore(color(lore.toList()))
        return this
    }

    fun texture(texture: String): SkullBuilder {
        val uuid = UUID.randomUUID()
        val playerProfile: PlayerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16))
        playerProfile.setProperty(ProfileProperty("textures", texture))
        meta.playerProfile = playerProfile
        return this
    }

    fun texture(player: Player): SkullBuilder {
        meta.setOwningPlayer(player)
        return this
    }

    fun texture(player: OfflinePlayer): SkullBuilder {
        meta.setOwningPlayer(player)
        return this
    }

    fun build(): ItemStack {
        itemStack.itemMeta = meta
        return itemStack
    }
}