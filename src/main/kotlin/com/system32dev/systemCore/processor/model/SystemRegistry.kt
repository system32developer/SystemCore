package com.system32dev.systemCore.processor.model

import org.bukkit.plugin.java.JavaPlugin

interface SystemRegistry {
    fun onLoad(plugin: JavaPlugin) {}
    fun onEnable(plugin: JavaPlugin) {}
    fun onDisable(plugin: JavaPlugin) {}
}