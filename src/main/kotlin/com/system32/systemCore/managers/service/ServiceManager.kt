package com.system32.systemCore.managers.service

import com.system32.systemCore.SystemCore
import java.util.ServiceLoader

object ServiceManager {
    private val services: ServiceLoader<PluginService> = ServiceLoader.load(
    PluginService::class.java,
        SystemCore::class.java.classLoader
    )

    init {
        println("Services count: ${services.count()}")
        services.forEach { it.onEnable() }
    }

    fun onDisable() {
        services.forEach { it.onDisable() }
    }

}