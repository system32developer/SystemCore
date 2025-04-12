package com.system32.systemCore.utils.cache

import com.system32.systemCore.SystemCore
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

/**
    * ExpiringCache is a simple cache implementation that stores key-value pairs with an expiration time.
    * It automatically removes expired entries and can be used to cache data that is only valid for a certain period.
    *
    * Example:
    *
    * ```kotlin
    * val cache = ExpiringCache<String, String>(plugin, 60) // 60 seconds expiry time
    *
    * cache.put("key", "value")
    *
    * val value = cache.get("key") // Returns "value" if within expiry time
    *
    * cache.remove("key") // Removes the entry
    *
    * cache.stop() // Stops the cleanup task
    *
    * ```
 **/

class ExpiringCache<K, V>(
    val expireIn: Int = 10,
    val cleanup: Int = 10
){
    private val cache = LinkedHashMap<K, Pair<Long, V>>()
    private var cleanupTask: BukkitTask? = null

    init {
        startCleanupTask()
    }

    @Synchronized
    fun put(key: K, value: V) {
        cache[key] = System.currentTimeMillis() to value
    }

    @Synchronized
    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        return if (System.currentTimeMillis() - entry.first < expireIn * 1000) {
            cache[key] = System.currentTimeMillis() to entry.second
            entry.second
        } else {
            cache.remove(key)
            null
        }
    }

    @Synchronized
    fun remove(key: K) {
        cache.remove(key)
    }

    @Synchronized
    private fun cleanup() {
        val now = System.currentTimeMillis()
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value.first >= expireIn * 1000) {
                iterator.remove()
            }
        }
    }

    private fun startCleanupTask() {
        cleanupTask = Bukkit.getScheduler().runTaskTimer(SystemCore.plugin, Runnable {
            cleanup()
        }, cleanup * 20L, expireIn * 20L)
    }

    fun stop() {
        cleanupTask?.cancel()
    }
}
