package com.system32.systemCore.utils.config

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

class ConfigUtil (private val plugin: JavaPlugin, fileName: String, insideOf: File? = null) : YamlConfiguration() {

    private val file: File = File(insideOf ?: plugin.dataFolder, fileName)

    init {
        if (!file.exists()) {
            file.parentFile.mkdirs()

            if (plugin.getResource(fileName) == null) {
                try {
                    file.createNewFile()
                } catch (ex: IOException) {
                    plugin.logger.severe("Failed to create new file $fileName")
                }
            } else {
                plugin.saveResource(fileName, false)
            }
        }

        reload()
    }

    fun save() {
        try {
            save(file)
        } catch (e: IOException) {
            plugin.logger.severe("Failed to save file ${file.name}")
        }
    }

    fun reload() {
        try {
            load(file)
        } catch (e: Exception) {
            e.printStackTrace()
            plugin.logger.severe("Failed to reload file ${file.name}")
        }
    }
}