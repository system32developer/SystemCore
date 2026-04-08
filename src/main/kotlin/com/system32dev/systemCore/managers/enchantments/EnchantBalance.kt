package com.system32dev.systemCore.managers.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry

object EnchantBalance {

    data class CostData(
        val minBase: Int,
        val step: Int,
        val maxOffset: Int
    )

    fun costData(maxLevel: Int): CostData = when (maxLevel) {
        1 -> CostData(
            minBase = 15,
            step = 0,
            maxOffset = 35
        )
        2 -> CostData(
            minBase = 5,
            step = 20,
            maxOffset = 32
        )
        3 -> CostData(
            minBase = 5,
            step = 10,
            maxOffset = 15
        )
        4 -> CostData(
            minBase = 1,
            step = 9,
            maxOffset = 35
        )
        else -> CostData(
            minBase = 1,
            step = 10,
            maxOffset = 20
        )
    }

    fun minCost(maxLevel: Int): EnchantmentRegistryEntry.EnchantmentCost {
        val d = costData(maxLevel)
        return EnchantmentRegistryEntry.EnchantmentCost.of(
            d.minBase,
            d.step
        )
    }

    fun maxCost(maxLevel: Int): EnchantmentRegistryEntry.EnchantmentCost {
        val d = costData(maxLevel)
        return EnchantmentRegistryEntry.EnchantmentCost.of(
            d.minBase + d.maxOffset,
            d.step
        )
    }

    fun weight(maxLevel: Int): Int = when (maxLevel) {
        1 -> 1
        2 -> 2
        3 -> 2
        4 -> 10
        else -> 12
    }

    fun anvilCost(maxLevel: Int): Int = when (maxLevel) {
        1 -> 8
        2 -> 10
        3 -> 4
        else -> 1
    }
}