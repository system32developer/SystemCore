package com.system32.systemCore.managers.config.serializers

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
        if (parts.size != 4) return null

        val worldName = parts[0]
        val world = Bukkit.getWorld(worldName) ?: return null
        val x = parts[1].toDoubleOrNull() ?: return null
        val y = parts[2].toDoubleOrNull() ?: return null
        val z = parts[3].toDoubleOrNull() ?: return null

        return Location(world, x, y, z)
    }

    override fun serialize(type: Type?, obj: Location?, node: ConfigurationNode?) {
        if (node == null) return
        if (obj == null) {
            node.set(null)
            println("No Location to save, setting node to null")
            return
        }

        val str = "${obj.world.name},${obj.x},${obj.y},${obj.z}"
        node.set(str)
    }
}