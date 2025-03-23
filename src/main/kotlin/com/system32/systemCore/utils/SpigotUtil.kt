package com.system32.systemCore.utils

import com.system32.systemCore.SystemCore
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.net.URL
import java.util.*


class SpigotUtil {

    companion object{

        fun player(uuid: UUID): Player? {
            return Bukkit.getPlayer(uuid)
        }

        fun player(name: String) : Player?{
            return Bukkit.getPlayer(name)
        }

        fun offlinePlayer(name: String) : Player?{
            return Bukkit.getOfflinePlayer(name).player
        }

        fun offlinePlayer(uuid: UUID) : Player?{
            return Bukkit.getOfflinePlayer(uuid).player
        }

        fun players() : List<Player>{
            return Bukkit.getOnlinePlayers().toList()
        }

        fun hearts(amount: Double): Double{
            return amount * 2
        }

        fun center(block: Block): Location {
            return block.location.add(0.5, 0.5, 0.5)
        }

        fun center(block: Block, block2: Block): Location {
            return block.location.add((block2.location.x - block.location.x) / 2, (block2.location.y - block.location.y) / 2, (block2.location.z - block.location.z) / 2)
        }
        /**
         * Checks if there is a new version of the plugin available on SpigotMC.
         *
         * @param onLatest (Optional) Action to execute if the plugin is up to date. Default is an empty function.
         * @param onOutdated Action to execute if a new version is available. It provides:
         *  - `latestVersion`: The latest version available on SpigotMC.
         *  - `downloadLink`: The direct link to the plugin's resource page on SpigotMC.
         *
         * ### Example usage:
         * ```
         * val spigotUtil = SpigotUtil(plugin, 12345) // SpigotMC resource ID
         * spigotUtil.version(
         *    onLatest = {
         *      plugin.logger.info("You are running the latest version.")
         *    },
         *     onOutdated = { latestVersion, downloadLink ->
         *      plugin.logger.info("A new version ($latestVersion) is available! Download it here: $downloadLink")
         *     }
         * )
         * ```
         */
        fun version(resourceId: Int, onLatest: () -> Unit = {}, onOutdated: (latestVersion: String, downloadLink: String) -> Unit) {
            val plugin: Plugin = SystemCore.plugin
            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                try {
                    URL("https://api.spigotmc.org/legacy/update.php?resource=$resourceId/~").openStream().use { inputStream ->
                        Scanner(inputStream).use { scanner ->
                            if (scanner.hasNext()) {
                                val latestVersion = scanner.next()
                                val currentVersion = plugin.pluginMeta.version
                                val downloadLink = "https://www.spigotmc.org/resources/$resourceId"

                                if (currentVersion == latestVersion) {
                                    onLatest()
                                } else {
                                    onOutdated(latestVersion, downloadLink)
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    plugin.logger.info("Unable to check for updates: ${e.message}")
                }
            })
        }

        fun metrics(id: Int, plugin: JavaPlugin) : Metrics{
            return Metrics(plugin, id)
        }
    }

}
