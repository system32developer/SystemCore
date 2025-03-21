package com.system32.systemCore.utils

import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.util.*


class SpigotUtil (private val plugin: JavaPlugin, private val resourceId: Int) {

    companion object{

        fun hearts(amount: Double): Double{
            return amount * 2
        }

        fun center(block: Block): Location {
            return block.location.add(0.5, 0.5, 0.5)
        }

        fun fromBase64ItemStack(base64: String): ItemStack? {
            try {
                val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(base64))
                val bukkitInputStream = BukkitObjectInputStream(inputStream)
                val item = bukkitInputStream.readObject() as ItemStack?
                bukkitInputStream.close()
                return item
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        fun toBase64(item: ItemStack?): String? {
            try {
                val outputStream = ByteArrayOutputStream()
                val bukkitOutputStream = BukkitObjectOutputStream(outputStream)
                bukkitOutputStream.writeObject(item)
                bukkitOutputStream.close()
                return Base64Coder.encodeLines(outputStream.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
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
    fun version(onLatest: () -> Unit = {}, onOutdated: (latestVersion: String, downloadLink: String) -> Unit) {
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

    fun metrics(id: Int) : Metrics{
        return Metrics(plugin, id)
    }

}
