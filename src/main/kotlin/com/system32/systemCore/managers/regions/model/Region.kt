package com.system32.systemCore.managers.regions.model

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

data class Region(
    val id: String,
    val world: String,
    val bounds: AABB
) {
    val players: MutableSet<UUID> = mutableSetOf()

    fun contains(location: Vector3): Boolean {
        return bounds.contains(location)
    }

    fun contains(x: Double, y: Double, z: Double): Boolean {
        return bounds.contains(Vector3(x, y, z))
    }

    fun contains(player: Player): Boolean {
        return player.world.name == world && contains(player.location.toVector3())
    }

    fun Location.toVector3(): Vector3 {
        return Vector3(x, y, z)
    }
}
