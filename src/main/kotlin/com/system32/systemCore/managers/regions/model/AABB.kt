package com.system32.systemCore.managers.regions.model

data class AABB(val min: Vector3, val max: Vector3) {

    companion object {
        fun of(a: Vector3, b: Vector3): AABB {
            return AABB(
                Vector3(
                    minOf(a.x, b.x),
                    minOf(a.y, b.y),
                    minOf(a.z, b.z)
                ),
                Vector3(
                    maxOf(a.x, b.x) + 1.0,
                    maxOf(a.y, b.y) + 1.0,
                    maxOf(a.z, b.z) + 1.0
                )
            )
        }
    }

    fun contains(point: Vector3): Boolean {
        return point.x >= min.x && point.x < max.x &&
                point.y >= min.y && point.y < max.y &&
                point.z >= min.z && point.z < max.z
    }
}
