package com.system32dev.systemCore.managers.config.serializers

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import kotlin.random.Random

data class Range<T : Number>(
    val min: T,
    val max: T
) {

    private val converter: (Double) -> T = buildConverter()

    @Suppress("UNCHECKED_CAST")
    private fun buildConverter(): (Double) -> T = when (min) {
        is Int -> ({ it.toInt() as T })
        is Long -> ({ it.toLong() as T })
        is Float -> ({ it.toFloat() as T })
        is Double -> ({ it as T })
        is Short -> ({ it.toInt().toShort() as T })
        is Byte -> ({ it.toInt().toByte() as T })
        else -> ({ it as T })
    }

    fun random(): T = converter(Random.nextDouble(min.toDouble(), max.toDouble()))

    fun contains(value: T): Boolean {
        return value.toDouble() >= min.toDouble() && value.toDouble() <= max.toDouble()
    }

    fun clamp(value: T): T {
        val v = value.toDouble()
        return when {
            v < min.toDouble() -> min
            v > max.toDouble() -> max
            else -> value
        }
    }

    fun size(): Double = max.toDouble() - min.toDouble()

    fun midpoint(): Double = (min.toDouble() + max.toDouble()) / 2.0

    fun overlaps(other: Range<*>): Boolean {
        return min.toDouble() <= other.max.toDouble() && max.toDouble() >= other.min.toDouble()
    }

    fun lerp(t: Double): Double {
        return min.toDouble() + (max.toDouble() - min.toDouble()) * t.coerceIn(0.0, 1.0)
    }

    class Serializer : TypeSerializer<Range<*>> {
        override fun deserialize(type: Type, node: ConfigurationNode): Range<*> {
            val min = node.node("min").get(Double::class.java) ?: 0.0
            val max = node.node("max").get(Double::class.java) ?: 0.0
            return Range(min, max)
        }

        override fun serialize(type: Type, obj: Range<*>?, node: ConfigurationNode) {
            if (obj == null) {
                node.set(null)
                return
            }
            node.node("min").set(obj.min)
            node.node("max").set(obj.max)
        }
    }
}