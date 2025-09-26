package com.system32.systemCore.managers.processor

interface PluginService {
    fun onLoad() {}
    fun onEnable()
    fun onDisable()
    fun onReload()
}