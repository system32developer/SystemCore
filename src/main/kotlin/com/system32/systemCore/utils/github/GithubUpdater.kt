package com.system32.systemCore.utils.github

import com.google.gson.JsonParser
import com.system32.systemCore.SystemCore
import com.system32.systemCore.utils.minecraft.ServerUtil.Companion.taskAsync
import org.bukkit.Bukkit
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import com.system32.systemCore.utils.OtherUtil.Companion.logger


class GithubUpdater {
    companion object {
        private val GITHUB_API_URL = "https://api.github.com/repos/${SystemCore.githubUser}/${SystemCore.githubRepo}/releases/latest"
        private val DOWNLOAD_URL = "https://github.com/${SystemCore.githubUser}/${SystemCore.githubRepo}/releases/download/"
    }
    private val plugins: MutableMap<String, String> = mutableMapOf()

    fun checkUpdates(
        onLatest: () -> Unit = {},
        needUpdate: (HashMap<String, String>) -> Unit,
        dontNeedUpdate: (HashMap<String, String>) -> Unit
    ) {
        taskAsync {
            try {
                val connection: URLConnection = URL(GITHUB_API_URL).openConnection().apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val json = JsonParser.parseReader(InputStreamReader(connection.getInputStream())).asJsonObject
                val tagName = json["tag_name"].asString
                plugins.clear()

                json["assets"].asJsonArray.forEach { element ->
                    val asset = element.asJsonObject
                    val nameParts = asset["name"].asString.split("-")
                    if (nameParts.size >= 2) {
                        val pluginName = nameParts[0]
                        val pluginVersion = nameParts[1].split(".j")[0]
                        plugins[pluginName] = pluginVersion
                    }
                }

                val updatesNeeded = hashMapOf<String, String>()
                val upToDate = hashMapOf<String, String>()
                var allPluginsLoaded = true

                for ((pluginName, latestVersion) in plugins) {
                    if (hasPlugin(pluginName)) {
                        val currentVersion = getPluginVersion(pluginName)
                        if (isOutdated(currentVersion, latestVersion)) {
                            updatesNeeded[pluginName] = latestVersion
                            allPluginsLoaded = false
                        } else {
                            upToDate[pluginName] = latestVersion
                        }
                    } else {
                        updatesNeeded[pluginName] = latestVersion
                        allPluginsLoaded = false
                    }
                }

                if (allPluginsLoaded && updatesNeeded.isEmpty()) {
                    onLatest()
                } else {
                    needUpdate(updatesNeeded)
                }

                dontNeedUpdate(upToDate)
            } catch (e: IOException) {
                logger.warning("Could not check for updates: ${e.message}")
            }
        }
    }

    private fun hasPlugin(pluginName: String): Boolean {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null
    }

    private fun getPluginVersion(pluginName: String): String {
        return Bukkit.getPluginManager().getPlugin(pluginName)?.pluginMeta!!.version
    }

    private fun isOutdated(currentVersion: String, latestVersion: String): Boolean {
        return currentVersion.replace(".", "").toInt() < latestVersion.replace(".", "").toInt()
    }

    fun downloadPlugin(pluginName: String, version: String, tagName: String) {
        taskAsync{
            try {
                val download = URL("$DOWNLOAD_URL$tagName/$pluginName-$version.jar")
                val path = Path("plugins/$pluginName.jar")
                Files.copy(download.openStream(), path, StandardCopyOption.REPLACE_EXISTING)
                logger.info("Successfully downloaded $pluginName v$version")
            } catch (e: IOException) {
                logger.warning("Failed to download $pluginName: ${e.message}")
            }
        }
    }
}