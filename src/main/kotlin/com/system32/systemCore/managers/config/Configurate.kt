package com.system32.systemCore.managers.config

import com.system32.systemCore.managers.config.adapters.ComponentAdapter
import com.system32.systemCore.managers.config.adapters.ComponentListAdapter
import com.system32.systemCore.managers.config.adapters.LocationAdapter
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus
import java.io.File
import kotlin.collections.iterator
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(val value: String)

interface ConfigAdapter<T> {
    fun serialize(value: T): Any
    fun deserialize(value: Any): T
}

object AdapterRegistry {
    private val adapters = mutableMapOf<KClass<*>, ConfigAdapter<*>>()

    init {
        register(Component::class, ComponentAdapter())
        register(Location::class, LocationAdapter())
        @Suppress("UNCHECKED_CAST")
        register(List::class as KClass<List<Component>>, ComponentListAdapter())
    }

    fun <T : Any> register(clazz: KClass<T>, adapter: ConfigAdapter<T>) {
        adapters[clazz] = adapter
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(clazz: KClass<T>): ConfigAdapter<T>? = adapters[clazz] as? ConfigAdapter<T>
}
@ApiStatus.Experimental
object Configurate {
    private val propertyCache = mutableMapOf<KClass<*>, List<KProperty1<Any, Any?>>>()
    private val constructorCache = mutableMapOf<KClass<*>, KFunction<Any>>()

    @Suppress("UNCHECKED_CAST")
    private fun getProperties(kClass: KClass<*>): List<KProperty1<Any, Any?>> {
        return propertyCache.getOrPut(kClass) {
            kClass.memberProperties.map { it as KProperty1<Any, Any?> }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getPrimaryConstructor(clazz: KClass<T>): KFunction<Any> {
        return constructorCache.getOrPut(clazz) {
            clazz.primaryConstructor as? KFunction<Any>
                ?: error("No primary constructor for \${clazz.simpleName}")
        }
    }

    fun <T : Any> load(plugin: JavaPlugin, clazz: KClass<T>): T {
        val annotation = clazz.findAnnotation<Config>() ?: error("Missing @Config annotation")
        val folder = plugin.dataFolder
        folder.mkdirs()
        val file = File(folder, "\${annotation.value}.yml")
        file.createNewFile()

        val config = YamlConfiguration.loadConfiguration(file)

        val defaultInstance = getPrimaryConstructor(clazz).callBy(emptyMap()) as T
        val flatSerializedDefaults = flattenMap(serializeConfig(defaultInstance))
        val existingKeys = config.getKeys(true).map { it.lowercase() }.toSet()
        val validKeys = flatSerializedDefaults.keys.toSet()

        var updated = false
        for ((key, value) in flatSerializedDefaults) {
            if (!config.contains(key) || config.get(key) == null) {
                config.set(key, value)
                updated = true
            }
        }

        val flattenExistingKeys = removeSection(existingKeys)
        for (key in flattenExistingKeys) {
            if (key !in validKeys) {
                config.set(key, null)
                updated = true
            }
        }

        if (updated) config.save(file)

        val map = config.getValues(true).mapKeys { it.key.lowercase() }
        return deserializeConfig(map, clazz)
    }

    fun <T : Any> save(plugin: JavaPlugin, instance: T) {
        val clazz = instance::class
        val annotation = clazz.findAnnotation<Config>() ?: error("Missing @Config annotation")
        val folder = plugin.dataFolder
        folder.mkdirs()
        val file = File(folder, "\${annotation.value}.yml")
        file.createNewFile()

        val serialized = serializeConfig(instance)
        val flat = flattenMap(serialized)

        val config = YamlConfiguration.loadConfiguration(file)
        for ((key, value) in flat) {
            config.set(key, value)
        }
        config.save(file)
    }

    fun <T : Any> reload(plugin: JavaPlugin, clazz: KClass<T>): T {
        return load(plugin, clazz)
    }

    private fun removeSection(set: Set<String>): Set<String> {
        val result = mutableSetOf<String>()
        val sectionsToRemove = mutableSetOf<String>()

        val potentialParents = set.filter { !it.contains(".") }
        for (parent in potentialParents) {
            val hasChildren = set.any { it.startsWith("\$parent.") }
            if (hasChildren) {
                sectionsToRemove.add(parent)
            }
        }

        result.addAll(set.filter { it !in sectionsToRemove })
        return result
    }

    private fun serializeConfig(instance: Any): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val kClass = instance::class

        for (property in getProperties(kClass)) {
            val value = property.get(instance) ?: continue
            val name = toYamlKey(property.name)
            val type = property.returnType.jvmErasure
            val adapter = AdapterRegistry.get(type)

            result[name] = when {
                adapter != null -> (adapter as ConfigAdapter<Any>).serialize(value)
                type.isData -> serializeConfig(value)
                else -> value
            }
        }

        return result
    }

    private fun <T : Any> deserializeConfig(map: Map<String, Any?>, clazz: KClass<T>): T {
        val constructor = getPrimaryConstructor(clazz)
        val args = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            val name = param.name ?: continue
            val key = toYamlKey(name)
            val raw = map[key]

            val type = param.type.jvmErasure
            val adapter = AdapterRegistry.get(type)

            args[param] = when {
                adapter != null && raw != null -> adapter.deserialize(raw)
                type.isData && raw is Map<*, *> -> deserializeConfig(raw as Map<String, Any?>, type)
                raw == null && param.isOptional -> continue
                else -> raw
            }
        }

        return constructor.callBy(args) as T
    }

    private fun toYamlKey(name: String): String {
        return name.replace(Regex("([a-z])([A-Z])"), "\$1-\$2").lowercase()
    }

    private fun flattenMap(map: Map<String, Any?>, prefix: String = ""): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        for ((key, value) in map) {
            val fullKey = if (prefix.isEmpty()) key else "\$prefix.\$key"
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                result.putAll(flattenMap(value as Map<String, Any?>, fullKey))
            } else {
                result[fullKey] = value
            }
        }
        return result
    }
}
