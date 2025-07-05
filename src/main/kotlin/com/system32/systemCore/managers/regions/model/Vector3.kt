package com.system32.systemCore.managers.regions.model

import org.bukkit.Location


data class Vector3(val x: Double, val y: Double, val z: Double){
    constructor(location: Location) : this(location.x, location.y, location.z)
}