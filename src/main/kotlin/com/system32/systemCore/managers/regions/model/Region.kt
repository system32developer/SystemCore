package com.system32.systemCore.managers.regions.model

import com.system32.systemCore.utils.minecraft.ServerUtil.taskTimer
import com.system32.systemCore.utils.text.TextUtil
import com.system32.systemCore.utils.text.TextUtil.color
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.UUID

data class Region(
    val id: String,
    val world: String,
    val bounds: BoundingBox
) {

    constructor(id: String, world: String, a: Location, b: Location) : this(
        id,
        world,
        BoundingBox(
            minOf(a.x, b.x),
            minOf(a.y, b.y),
            minOf(a.z, b.z),
            maxOf(a.x, b.x) + 1.0,
            maxOf(a.y, b.y) + 1.0,
            maxOf(a.z, b.z) + 1.0
        )
    )

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

    fun getEdges(min: Vector, max: Vector): List<Pair<Vector, Vector>> {
        val xs = listOf(min.x, max.x)
        val ys = listOf(min.y, max.y)
        val zs = listOf(min.z, max.z)

        val edges = mutableListOf<Pair<Vector, Vector>>()

        for (y in ys) for (z in zs) {
            edges += Vector(min.x, y, z) to Vector(max.x, y, z)
        }
        for (x in xs) for (z in zs) {
            edges += Vector(x, min.y, z) to Vector(x, max.y, z)
        }
        for (x in xs) for (y in ys) {
            edges += Vector(x, y, min.z) to Vector(x, y, max.z)
        }

        return edges
    }

    private fun spawnLine(world: World, start: Vector, end: Vector, step: Double, particle: Particle) {
        val dir = end.clone().subtract(start)
        val length = dir.length()
        dir.normalize()

        val points = (length / step).toInt()
        for (i in 0..points) {
            val point = start.clone().add(dir.clone().multiply(i * step))
            world.spawnParticle(particle, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    fun show(
        particle: Particle = Particle.HAPPY_VILLAGER,
        step: Double = 1.0,
        duration: Int = 3
    ) {
        val world = Bukkit.getWorld(world) ?: return
        val min = bounds.min
        val max = bounds.max
        val edges = getEdges(min, max)

        var elapsed = 0
        taskTimer(0, 1) {
            if (elapsed >= duration) {
                it.cancel()
                return@taskTimer
            }

            edges.forEach { (start, end) ->
                spawnLine(world, start, end, step, particle)
            }
            elapsed++
        }
    }
}
