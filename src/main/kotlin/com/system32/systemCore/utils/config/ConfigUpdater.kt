package com.system32.systemCore.utils.config

import com.tchristofferson.configupdater.ConfigUpdater
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ConfigUpdater {
    companion object{
        fun update(plugin: JavaPlugin, string: String, ignored: List<String> = emptyList()) {
            val file = File(plugin.dataFolder, string)
            ConfigUpdater.update(plugin, string, file, ignored)
        }
        fun update(plugin: JavaPlugin, preset: String, target: String, ignored: List<String> = emptyList()) {
            val file = File(plugin.dataFolder, target)
            ConfigUpdater.update(plugin, preset, file, ignored)
        }
    }
}