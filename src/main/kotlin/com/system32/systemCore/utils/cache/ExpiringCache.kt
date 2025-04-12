package com.system32.systemCore.utils.cache

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
    private val plugin: Plugin,
    expiryTimeMillis: Int,
    cleanupIntervalTicks: Int = 200
){
    private val expiryTimeMillis: Long = expiryTimeMillis.toLong()
    private val cleanupIntervalTicks: Long = cleanupIntervalTicks.toLong()
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
        return if (System.currentTimeMillis() - entry.first < expiryTimeMillis) {
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
            if (now - entry.value.first >= expiryTimeMillis) {
                iterator.remove()
            }
        }
    }

    private fun startCleanupTask() {
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            cleanup()
        }, cleanupIntervalTicks, cleanupIntervalTicks)
    }

    fun stop() {
        cleanupTask?.cancel()
    }
}
