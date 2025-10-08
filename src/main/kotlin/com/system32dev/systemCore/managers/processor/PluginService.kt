package com.system32dev.systemCore.managers.processor

interface PluginService {
    fun onLoad() {}
    fun onEnable()
    fun onDisable()
    fun onReload()
}