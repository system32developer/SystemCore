package com.system32.systemCore.managers.processor

interface PluginService {
    fun onEnable()
    fun onDisable()
    fun reload()
}