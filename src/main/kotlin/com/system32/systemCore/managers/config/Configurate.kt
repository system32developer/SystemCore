package com.system32.systemCore.managers.config

import com.system32.systemCore.managers.config.adapters.ComponentAdapter
import com.system32.systemCore.managers.config.adapters.ComponentListAdapter
import com.system32.systemCore.managers.config.adapters.LocationAdapter
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.collections.iterator
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
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

object Configurate {
    fun <T : Any> load(plugin: JavaPlugin, clazz: KClass<T>): T {
        val annotation = clazz.findAnnotation<Config>() ?: error("Missing @Config annotation")
        val folder = plugin.dataFolder
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, "${annotation.value}.yml")
        if (!file.exists()) file.createNewFile()

        val config = YamlConfiguration.loadConfiguration(file)

        val defaultInstance = clazz.primaryConstructor?.callBy(emptyMap()) ?: error("Could not create default instance of ${clazz.simpleName}")

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
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, "${annotation.value}.yml")
        if (!file.exists()) file.createNewFile()

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
            val hasChildren = set.any { it.startsWith("$parent.") }
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

        for (property in kClass.memberProperties) {
            @Suppress("UNCHECKED_CAST")
            val value = (property as KProperty1<Any, Any?>).get(instance)


            if (value == null) continue

            val name = toYamlKey(property.name)
            val type = property.returnType.jvmErasure
            val adapter = AdapterRegistry.get(type)

            if (adapter != null) {
                @Suppress("UNCHECKED_CAST")
                val castedAdapter = adapter as ConfigAdapter<Any>
                result[name] = castedAdapter.serialize(value)
            } else if (type.isData) {
                result[name] = serializeConfig(value)
            } else {
                result[name] = value
            }
        }

        return result
    }

    private fun <T : Any> deserializeConfig(map: Map<String, Any?>, clazz: KClass<T>): T {
        val constructor = clazz.primaryConstructor ?: error("No primary constructor for ${clazz.simpleName}")
        val args = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            val name = param.name ?: continue
            val key = toYamlKey(name)
            val raw = map[key]

            val type = param.type.jvmErasure
            val adapter = AdapterRegistry.get(type)

            if (adapter != null) {
                if (raw != null) {
                    args[param] = adapter.deserialize(raw)
                } else if (param.isOptional) {
                } else {
                    args[param] = null
                }
            } else if (type.isData) {
                if (raw is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    args[param] = deserializeConfig(raw as Map<String, Any?>, type)
                } else if (param.isOptional) {
                } else {
                    args[param] = null
                }
            } else {
                if (raw == null && param.isOptional) {
                } else {
                    args[param] = raw
                }
            }
        }
        return constructor.callBy(args)
    }

    private fun toYamlKey(name: String): String {
        return name.replace(Regex("([a-z])([A-Z])"), "$1-$2").lowercase()
    }

    private fun flattenMap(map: Map<String, Any?>, prefix: String = ""): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        for ((key, value) in map) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
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