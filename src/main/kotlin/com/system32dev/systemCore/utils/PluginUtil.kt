package com.system32dev.systemCore.utils

import com.system32dev.systemCore.Metrics
import com.system32dev.systemCore.SystemCore
import com.system32dev.systemCore.utils.tasks.taskAsync
import org.bukkit.NamespacedKey
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.net.URL
import java.util.Scanner

fun version(resourceId: Int, onLatest: () -> Unit = {}, onOutdated: (latestVersion: String, downloadLink: String) -> Unit) {
    taskAsync {
        try {
            URL("https://api.spigotmc.org/legacy/update.php?resource=$resourceId/~").openStream().use { inputStream ->
                Scanner(inputStream).use { scanner ->
                    if (scanner.hasNext()) {
                        val latestVersion = scanner.next()
                        val currentVersion = SystemCore.plugin.pluginMeta.version
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
            SystemCore.plugin.logger.info("Unable to check for updates: ${e.message}")
        }
    }
}

fun metrics(id: Int, plugin: JavaPlugin) : Metrics {
    return Metrics(plugin, id)
}

fun key(key: String): NamespacedKey {
    return NamespacedKey(SystemCore.plugin, key)
}