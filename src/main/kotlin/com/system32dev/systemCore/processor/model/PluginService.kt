package com.system32dev.systemCore.processor.model

interface PluginService {
    fun onLoad() {}
    fun onEnable()
    fun onDisable()
    fun onReload()
}