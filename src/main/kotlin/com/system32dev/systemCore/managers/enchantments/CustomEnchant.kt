package com.system32dev.systemCore.managers.enchantments

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.ItemTypeKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import io.papermc.paper.registry.set.RegistryKeySet
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemType
import java.util.function.Consumer

/**
 * Represents a custom enchantment that can be applied to items in Minecraft. This class provides the necessary properties and methods to define and register a custom enchantment. First register an context with
 *```kotlin
 * data class BlockUseContext(
 *     val player: Player,
 *     val block: Block,
 *     override val level: Int
 * ) : EnchantContext
 *```
 * then you can use it on your own enchants
 *```kotlin
 * interface BlockEnchant {
 *     fun handle(context: BlockUseContext)
 * }
 *```
 * Example:
 *```kotlin
 * object ArcheologyEnchant : CustomEnchant(
 *     id = "archeology",
 *     name = Component.text("Arqueólogo"),
 *     maxLevel = 5,
 *     supportedItems = setOf(ItemTypeKeys.BRUSH)
 * ), BlockEnchant {
 *     override fun handle(context: BlockUseContext) {
 *         context.player.sendMessage("You have used archeology level ${context.level}")
 *     }
 * }
 * ```
 */
abstract class CustomEnchant(
    val id: String,
    val name: Component,
    val maxLevel: Int,
    val supportedItems: Set<TypedKey<ItemType>>,
    val exclusiveWith: Set<String> = emptySet()
) {

    private lateinit var key: Key

    fun init(namespace: String) {
        key = Key.key(namespace, id)
    }

    private fun requireInit() {
        check(::key.isInitialized) {
            "CustomEnchant '$id' was not initialized"
        }
    }

    fun enchantKey(): TypedKey<Enchantment> {
        requireInit()
        return EnchantmentKeys.create(key)
    }

    val enchantment: Enchantment get() = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(enchantKey())!!

    fun build(): Consumer<EnchantmentRegistryEntry.Builder> = Consumer { b ->
        requireInit()
        b.description(name)
            .maxLevel(maxLevel)
            .weight(EnchantBalance.weight(maxLevel))
            .anvilCost(EnchantBalance.anvilCost(maxLevel))
            .minimumCost(EnchantBalance.minCost(maxLevel))
            .maximumCost(EnchantBalance.maxCost(maxLevel))
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
            .activeSlots(EquipmentSlotGroup.ANY)
            .supportedItems(RegistrySet.keySet(RegistryKey.ITEM, supportedItems))


        if (exclusiveWith.isNotEmpty()) {
            b.exclusiveWith(
                RegistrySet.keySet(
                    RegistryKey.ENCHANTMENT,
                    exclusiveWith.map {
                        EnchantmentKeys.create(Key.key(key.namespace(), it))
                    }
                )
            )
        }
    }
}