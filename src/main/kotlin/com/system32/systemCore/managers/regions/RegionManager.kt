package com.system32.systemCore.managers.regions

import com.system32.systemCore.SystemCore
import com.system32.systemCore.managers.regions.events.RegionEnteredEvent
import com.system32.systemCore.managers.regions.events.RegionLeftEvent
import com.system32.systemCore.managers.regions.model.Region
import com.system32.systemCore.managers.regions.model.RegionTree
import com.system32.systemCore.utils.minecraft.ServerUtil.taskLater
import com.system32.systemCore.utils.minecraft.SpigotUtil.center
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID

object RegionManager : Listener {
    private val trees = mutableMapOf<String, RegionTree>()
    private val playerRegions = mutableMapOf<UUID, Set<String>>()

    fun register(vararg regions: Region) {
        regions.forEach { region ->
            val tree = trees.computeIfAbsent(region.world) { RegionTree() }
            tree.insert(region)
        }
    }

    fun unregister(vararg regions: Region, condition: ((Region) -> Boolean)? = null) {
        regions.forEach { region ->
            if (condition != null && !condition(region)) return@forEach

            val tree = trees[region.world] ?: return@forEach
            tree.remove(region)
            if (tree.isEmpty()) {
                trees.remove(region.world)
            }
        }
    }

    fun unregister(vararg regions: String) {
        regions.forEach { regionId ->
            val region = getRegionById(regionId) ?: return@forEach
            unregister(region)
        }
    }


    fun getRegionsAt(world: String, x: Double, y: Double, z: Double): List<Region> {
        return trees[world]?.query(x, y, z) ?: emptyList()
    }

    fun getRegionsAt(player: Player): List<Region> {
        return getRegionsAt(player.location)
    }

    fun getRegionsAt(block: Block): List<Region> {
        return getRegionsAt(block.location)
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
    private fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from.block == event.to.block) return
        val from = center(event.from.block).subtract(0.0, 0.5, 0.0)
        from.yaw = event.player.yaw
        from.pitch = event.player.pitch
        handlePlayerMovement(event.player, event.to, event, from)
    }

    @EventHandler
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.from.block == event.to.block) return
        val from = center(event.from.block).subtract(0.0, 0.5, 0.0)
        from.yaw = event.player.yaw
        from.pitch = event.player.pitch
        handlePlayerMovement(event.player, event.to, event, from)
    }


    private fun handlePlayerMovement(player: Player, to: Location, event: Cancellable, from: Location) {

        val uuid = player.uniqueId
        val currentRegions = getRegionsAt(to)
        val currentIds = currentRegions.map { it.id }.toSet()
        val previousRegions = playerRegions[uuid] ?: emptySet()

        val acceptedEnteredIds = mutableSetOf<String>()
        val entered = currentRegions.filter { it.id !in previousRegions }

        for (region in entered) {
            val called = RegionEnteredEvent(player, region).call()
            if (called.isCancelled) {
                event.isCancelled = true
                taskLater(1L){
                    player.teleport(from)
                }
                continue
            }
            region.players.add(uuid)
            acceptedEnteredIds.add(region.id)
        }

        val acceptedExitedIds = mutableSetOf<String>()
        val exited = previousRegions.filter { id -> currentIds.none { it == id } }

        for (regionId in exited) {
            val region = getRegionById(regionId) ?: continue
            val called = RegionLeftEvent(player, region).call()
            if (called.isCancelled) {
                event.isCancelled = true
                taskLater(1L){
                    player.teleport(from)
                }
                continue
            }
            region.players.remove(uuid)
            acceptedExitedIds.add(regionId)
        }

        playerRegions[uuid] = ((previousRegions + acceptedEnteredIds) - acceptedExitedIds)
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val location = player.location

        val currentRegions = getRegionsAt(location)
        val currentIds = currentRegions.map { it.id }.toSet()

        playerRegions[uuid] = currentIds

        for (region in currentRegions) {

            val called = RegionEnteredEvent(player, region).call()
            if(called.isCancelled){
                continue
            }
            region.players.add(uuid)
        }
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId

        val regionIds = playerRegions.remove(uuid) ?: return

        for (regionId in regionIds) {
            val region = getRegionById(regionId) ?: continue

            val called = RegionLeftEvent(player, region).call()

            if(called.isCancelled){
                continue
            }
            region.players.remove(uuid)
        }
    }


    private fun <T : Event> T.call(): T {
        Bukkit.getPluginManager().callEvent(this)
        return this
    }
}
