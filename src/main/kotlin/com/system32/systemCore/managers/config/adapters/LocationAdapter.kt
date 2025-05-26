package com.system32.systemCore.managers.config.adapters

import com.system32.systemCore.managers.config.ConfigAdapter
import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.collections.get

class LocationAdapter : ConfigAdapter<Location> {
    override fun serialize(value: Location): Any {
        return mapOf(
            "world" to value.world?.name,
            "x" to value.x,
            "y" to value.y,
            "z" to value.z,
            "yaw" to value.yaw,
            "pitch" to value.pitch
        )
    }

    override fun deserialize(value: Any): Location {
        val map = value as Map<*, *>
        val world = Bukkit.getWorld(map["world"] as String)
        val x = (map["x"] as Number).toDouble()
        val y = (map["y"] as Number).toDouble()
        val z = (map["z"] as Number).toDouble()
        val yaw = (map["yaw"] as Number).toFloat()
        val pitch = (map["pitch"] as Number).toFloat()
        return Location(world, x, y, z, yaw, pitch)
    }
}