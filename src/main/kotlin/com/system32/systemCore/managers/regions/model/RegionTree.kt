package com.system32.systemCore.managers.regions.model

class RegionTree {
    private val regions = mutableListOf<Region>()

    fun insert(region: Region) {
        regions.add(region)
    }

    fun query(x: Double, y: Double, z: Double): List<Region> {
        return regions.filter { it.contains(x, y, z) }
    }

    fun queryBox(box: AABB): List<Region> {
        return regions.filter { it.bounds.intersects(box) }
    }

    fun getAllRegions(): List<Region> = regions
}
