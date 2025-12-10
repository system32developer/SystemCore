package com.system32dev.systemCore.managers.config.serializers

import org.bukkit.Bukkit
import org.bukkit.Location
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

class LocationSerializer : TypeSerializer<Location> {

    override fun deserialize(type: Type?, node: ConfigurationNode?): Location? {
        if (node == null || node.string == null) return null
        val raw = node.string!!
        val parts = raw.split(",")
        if (parts.size !in 4..6) {
            println("Invalid Location format: $raw")
            return null
        }

        val worldName = parts[0]
        val world = Bukkit.getWorld(worldName) ?: return null
        val x = parts[1].toDoubleOrNull() ?: return null
        val y = parts[2].toDoubleOrNull() ?: return null
        val z = parts[3].toDoubleOrNull() ?: return null

        val pitch = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0
        val yaw = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0

        if (parts.size > 4) {
            return Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())
        }

        return Location(world, x, y, z)
    }

    override fun serialize(type: Type?, obj: Location?, node: ConfigurationNode?) {
        if (node == null) return
        if (obj == null) {
            node.set(null)
            println("No Location to save, setting node to null")
            return
        }

        val yaw = obj.yaw
        val pitch = obj.pitch

        if (yaw == 0f && pitch == 0f) {
            val str = "${obj.world.name},${obj.x},${obj.y},${obj.z}"
            node.set(str)
            return
        }
        val str = "${obj.world.name},${obj.x},${obj.y},${obj.z},${pitch},${yaw}"
        node.set(str)
    }
}