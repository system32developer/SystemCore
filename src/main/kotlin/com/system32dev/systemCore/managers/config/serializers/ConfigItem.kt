package com.system32dev.systemCore.managers.config.serializers

import com.destroystokyo.paper.profile.ProfileProperty
import com.system32dev.systemCore.utils.color
import dev.triumphteam.gui.builder.item.ItemBuilder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.util.Base64
import java.util.UUID

data class ConfigItem(
    var amount: Int = 1,
    var material: String = "STONE",
    var name: String = "",
    var lore: List<String> = listOf(),
    var flags: List<String> = listOf(),
    //format is base:texture, texture:texture or owner:playername
    var skullTexture: String = "",
    var glow: Boolean = false,
    var customModelData: Int = 0,

    //gui stuff
    val actions: MutableList<String> = mutableListOf(),
    val slot: List<Int> = listOf()
) {
    fun build(tags: TagResolver? = null): ItemStack? {
        val item = ItemStack(Material.getMaterial(material) ?: return null, amount)
        val meta = item.itemMeta
        if(name.isNotEmpty()) meta.displayName(color(name, tags))
        if(lore.isNotEmpty()) meta.lore(lore.map { color(it, tags) })
        if(meta is SkullMeta){
            val prefix = skullTexture.split(":").getOrNull(0) ?: ""
            val value = skullTexture.split(":").getOrNull(1) ?: ""
            if(prefix.isNotEmpty() && value.isNotEmpty()){
                when(prefix.lowercase()){
                    "texture" -> {
                        val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(value.toByteArray()))
                        val encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skullTexture).toByteArray())
                        profile.properties.add(ProfileProperty("textures", String(encodedData)))
                        meta.playerProfile = profile
                    }
                    "base" -> {
                        val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(value.toByteArray()))
                        profile.properties.add(ProfileProperty("textures", value))
                        meta.playerProfile = profile
                    }
                    "owner" -> meta.owningPlayer = Bukkit.getOfflinePlayer(value)
                }
            }
        }
        if(flags.isNotEmpty()) flags.forEach { flag ->
            try {
                val enumFlag = ItemFlag.valueOf(flag)
                meta.addItemFlags(enumFlag)
            } catch (e: Exception) {
                //ignore
            }
        }
        if(glow) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS).also { item.addUnsafeEnchantment(Enchantment.LURE, 1) }
        }
        if(customModelData != 0) meta.setCustomModelData(customModelData)
        item.itemMeta = meta
        return item
    }

    fun clone() = ConfigItem(amount, material, name, lore.toList(), flags.toList(), skullTexture, glow, customModelData)

    fun toItemBuilder(tags: TagResolver? = null) = ItemBuilder.from(build(tags)!!)

    fun toGuiItem(tags: TagResolver? = null, onClick: (InventoryClickEvent) -> Unit) = toItemBuilder(tags).asGuiItem { onClick(it) }

    class Serializer : TypeSerializer<ConfigItem> {
        override fun deserialize(
            type: Type,
            node: ConfigurationNode
        ): ConfigItem {

            val amount = node.node("amount").getInt(1)
            val material = node.node("material").getString("STONE")
            val name = node.node("name").getString("")
            val lore = node.node("lore").childrenList().map { it.getString("") }
            val flags = node.node("flags").childrenList().map { it.getString("") }
            val skullTexture = node.node("skullTexture").getString("") ?: ""
            val glow = node.node("glow").getBoolean(false)
            val customModelData = node.node("customModelData").getInt(0)
            val actions = node.node("actions").getList(String::class.java)?.toMutableList() ?: mutableListOf()
            val slot = node.node("slot").getList(Int::class.javaObjectType)?.toList() ?: listOf()
            return ConfigItem(
                amount = amount,
                material = material,
                name = name,
                lore = lore,
                flags = flags,
                skullTexture = skullTexture,
                glow = glow,
                customModelData = customModelData,
                actions = actions,
                slot = slot
            )
        }

        override fun serialize(type: Type, obj: ConfigItem?, node: ConfigurationNode) {
            if (obj == null) {
                node.set(null)
                return
            }
            if (obj.amount != 1) node.node("amount").set(obj.amount) else node.node("amount").set(null)
            if (obj.material != "STONE") node.node("material").set(obj.material) else node.node("material").set(null)
            if (obj.name.isNotEmpty()) node.node("name").set(obj.name) else node.node("name").set(null)
            if (obj.lore.isNotEmpty()) node.node("lore").setList(String::class.java, obj.lore) else node.node("lore").set(null)
            if (obj.flags.isNotEmpty()) node.node("flags").setList(String::class.java, obj.flags) else node.node("flags").set(null)
            if (obj.skullTexture.isEmpty()) node.node("skullTexture").set(null) else node.node("skullTexture").set(obj.skullTexture)
            if (!obj.glow) node.node("glow").set(null) else node.node("glow").set(obj.glow)
            if (obj.customModelData != 0) node.node("customModelData").set(obj.customModelData) else node.node("customModelData").set(null)
            if (obj.actions.isNotEmpty()) node.node("actions").setList(String::class.java, obj.actions) else node.node("actions").set(null)
            if(obj.slot.isNotEmpty()) node.node("slot").setList(Int::class.java,obj.slot) else node.node("slot").set(null)
        }
    }
}