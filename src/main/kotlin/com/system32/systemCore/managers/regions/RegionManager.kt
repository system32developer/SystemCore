package com.system32.systemCore.managers.regions

import com.system32.systemCore.SystemCore
import com.system32.systemCore.managers.regions.events.RegionEnteredEvent
import com.system32.systemCore.managers.regions.events.RegionLeftEvent
import com.system32.systemCore.managers.regions.model.AABB
import com.system32.systemCore.managers.regions.model.Region
import com.system32.systemCore.managers.regions.model.RegionTree
import com.system32.systemCore.managers.regions.model.Vector3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID

object RegionManager : Listener {
    private val trees = mutableMapOf<String, RegionTree>()
    private val playerRegions = mutableMapOf<UUID, Set<String>>()

    fun registerRegion(region: Region) {
        val tree = trees.computeIfAbsent(region.world) { RegionTree() }
        tree.insert(region)
    }

    fun getRegionsAt(world: String, x: Double, y: Double, z: Double): List<Region> {
        return trees[world]?.query(x, y, z) ?: emptyList()
    }

    fun getRegionsAt(location: Location): List<Region> {
        return getRegionsAt(location.world?.name ?: return emptyList(), location.x, location.y, location.z)
    }

    fun getRegionById(id: String): Region? {
        return trees.values
            .flatMap { it.getAllRegions() }
            .firstOrNull { it.id == id }
    }

    init {
        SystemCore.event(this)
    }


    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if(event.from.block == event.to.block) return

        val current = event.to
        val uuid = player.uniqueId
        val currentRegions = getRegionsAt(current)
        val currentIds = currentRegions.map { it.id }.toSet()
        val previousRegions = playerRegions[uuid] ?: emptySet()

        val entered = currentRegions.filter { it.id !in previousRegions }
        for (region in entered) {
            val event = RegionEnteredEvent(player, region).call()
            if(event.isCancelled){
                event.isCancelled = true
                continue
            }
            region.players.add(uuid)
        }

        val exited = previousRegions.filter { id -> currentIds.none { id == it } }
        for (regionId in exited) {
            val region = getRegionById(regionId)?: continue
            val event = RegionLeftEvent(player, region).call()
            if(event.isCancelled){
                event.isCancelled = true
                continue
            }
            region.players.remove(uuid)

        }

        playerRegions[uuid] = currentIds
    }

    fun <T : Event> T.call(): T {
        Bukkit.getPluginManager().callEvent(this)
        return this
    }
}
