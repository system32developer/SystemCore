package com.system32.systemCore.managers.regions.model

data class AABB(val min: Vector3, val max: Vector3) {
    fun contains(point: Vector3): Boolean {
        return point.x >= min.x && point.x <= max.x &&
                point.y >= min.y && point.y <= max.y &&
                point.z >= min.z && point.z <= max.z
    }

    fun intersects(other: AABB): Boolean {
        return !(other.min.x > max.x || other.max.x < min.x ||
                other.min.y > max.y || other.max.y < min.y ||
                other.min.z > max.z || other.max.z < min.z)
    }
}