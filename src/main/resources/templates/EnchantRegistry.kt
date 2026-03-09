package com.system32dev.systemCore.generated

import com.system32dev.systemCore.managers.enchantments.CustomEnchant
import com.system32dev.systemCore.managers.enchantments.EnchantContext
import com.system32dev.systemCore.processor.annotations.AutoRegistry
import org.bukkit.inventory.ItemStack
import com.system32dev.systemCore.processor.model.SystemRegistry
import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass

@AutoRegistry
object EnchantRegistry: SystemRegistry {

    val enchants: MutableMap<KClass<out CustomEnchant>, CustomEnchant> = mutableMapOf(
        {{enchants}}
    )

    fun init(namespace: String){
        enchants.values.forEach { it.init(namespace.lowercase()) }
    }

    /**
     * Example:
     *
     *```kotlin
     * EnchantRegistry.withEnchant<Impact, BlockUseContext>(
     *             item,
     *             { level -> BlockUseContext(player, block, level) }
     * ) { enchant, context -> enchant.handle(context) }}
     *```
     */
    inline fun <reified E : CustomEnchant, C : EnchantContext> EnchantRegistry.withEnchant(
        item: ItemStack,
        levelToContext: (level: Int) -> C,
        block: (E, C) -> Unit
    ) {
        val enchant = enchants[E::class] as? E ?: return
        val level = item.getEnchantmentLevel(enchant.enchantment)
        if (level <= 0) return

        block(enchant, levelToContext(level))
    }

    override fun onLoad(plugin: JavaPlugin) {}
    override fun onEnable(plugin: JavaPlugin) {}
    override fun onDisable(plugin: JavaPlugin) {}
}