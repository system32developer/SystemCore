package com.system32.systemCore.utils

import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.net.URL
import java.util.Scanner
import java.util.function.Consumer

class SpigotUtil (private val plugin: JavaPlugin, private val resourceId: Int) {

    fun version(consumer: Consumer<String>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                URL("https://api.spigotmc.org/legacy/update.php?resource=$resourceId/~").openStream()
                    .use { inputStream ->
                        Scanner(inputStream).use { scanner ->
                            if (scanner.hasNext()) {
                                consumer.accept(scanner.next())
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
