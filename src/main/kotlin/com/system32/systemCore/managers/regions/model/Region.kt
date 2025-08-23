package com.system32.systemCore.managers.regions.model

import com.system32.systemCore.utils.text.TextUtil
import com.system32.systemCore.utils.text.TextUtil.color
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.UUID

data class Region(
    val id: String,
    val world: String,
    val bounds: BoundingBox
) {

    constructor(id: String, world: String, min: Location, max: Location) : this(id, world, BoundingBox.of(min, max))

    val players: MutableSet<UUID> = mutableSetOf()

    fun contains(location: Location): Boolean {
        return bounds.contains(location.toVector())
    }

    fun contains(x: Double, y: Double, z: Double): Boolean {
        return bounds.contains(Vector(x, y, z))
    }

    fun contains(player: Player): Boolean {
        return player.world.name == world && contains(player.location)
    }

    fun show(particle: Particle = Particle.HAPPY_VILLAGER, step: Double = 1.0){
        val world = Bukkit.getWorld(world) ?: return
        val min = bounds.min
        val max = bounds.max
        for (x in min.x.toInt()..max.x.toInt() step step.toInt()) {
            for (y in min.y.toInt()..max.y.toInt() step step.toInt()) {
                for (z in min.z.toInt()..max.z.toInt() step step.toInt()) {
                    val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                    world.spawnParticle(particle, location, 1)
                }
            }
        }
    }
}
