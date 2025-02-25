package com.system32.systemCore.utils

import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.net.URL
import java.util.Scanner
import java.util.function.Consumer

class SpigotUtil (private val plugin: JavaPlugin, private val resourceId: Int) {

    /**
     * Checks if there is a new version of the plugin available on SpigotMC.
     *
     * @param onLatest Action to execute if the plugin is up to date.
     * @param onOutdated Action to execute if a new version is available, receives the latest version as a parameter.
     *
     * ### Example usage:
     * ```
     * val spigotUtil = SpigotUtil(plugin, 12345) // SpigotMC resource ID
     * spigotUtil.version(
     *     onLatest = {
     *         plugin.logger.info("You are using the latest version.")
     *     },
     *     onOutdated = { latestVersion ->
     *         plugin.logger.info("A new version is available: $latestVersion. Please update!")
     *     }
     * )
     * ```
     */
    fun version(onLatest: () -> Unit, onOutdated: (String) -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                URL("https://api.spigotmc.org/legacy/update.php?resource=$resourceId/~").openStream().use { inputStream ->
                    Scanner(inputStream).use { scanner ->
                        if (scanner.hasNext()) {
                            val latestVersion = scanner.next()
                            val currentVersion = plugin.pluginMeta.version

                            if (currentVersion == latestVersion) {
                                onLatest()
                            } else {
                                onOutdated(latestVersion)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                plugin.logger.info("Unable to check for updates: ${e.message}")
            }
        })
    }

    fun metrics() : Metrics{
        return Metrics(plugin, 12345)
    }
}
