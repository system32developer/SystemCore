package com.system32.systemCore.managers.config


import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass
import com.system32.systemCore.utils.minecraft.ServerUtil.task
import com.system32.systemCore.utils.minecraft.ServerUtil.taskAsync
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Experimental
object ConfigManager {
    private lateinit var plugin: JavaPlugin
    private val containers = mutableMapOf<KClass<*>, Any>()

    fun initialize(plugin: JavaPlugin, vararg configs: KClass<*>, onComplete: (() -> Unit)? = null) {
        this.plugin = plugin

        if (configs.isEmpty()) {
            onComplete?.invoke()
            return
        }

        val total = configs.size
        var loadedCount = 0

        for (clazz in configs) {
            loadAsync(plugin, clazz) { config ->
                containers[clazz] = config
                loadedCount++
                if (loadedCount == total) {
                    onComplete?.invoke()
                }
            }
        }
    }

    fun <T : Any> loadAsync(plugin: JavaPlugin, clazz: KClass<T>, onComplete: (T) -> Unit) {
        taskAsync {
            val startLoadFile = System.nanoTime()
            val configInstance = Configurate.load(plugin, clazz)
            val endLoadFile = System.nanoTime()

            val elapsedLoadFile = (endLoadFile - startLoadFile) / 1_000_000.0

            task {
                plugin.logger.info("[ConfigManager] ${clazz.simpleName} load took $elapsedLoadFile ms ( ${elapsedLoadFile / 1000.0})s")
                onComplete(configInstance)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(clazz: KClass<T>): T? = containers[clazz] as? T

    fun <T : Any> save(clazz: KClass<T>) {
        val instance = containers[clazz] ?: error("${clazz.simpleName} not loaded")
        Configurate.save(plugin, instance)
    }

    fun <T : Any> reload(clazz: KClass<T>) {
        val newInstance = Configurate.reload(plugin, clazz)
        containers[clazz] = newInstance
    }
}
