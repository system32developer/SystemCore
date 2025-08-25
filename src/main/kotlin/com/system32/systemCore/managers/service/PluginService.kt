package com.system32.systemCore.managers.service

interface PluginService {
    fun onEnable()
    fun onDisable()
    fun reload()
}