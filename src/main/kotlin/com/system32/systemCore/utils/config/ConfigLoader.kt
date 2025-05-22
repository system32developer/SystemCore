package com.system32.systemCore.utils.config

import com.system32.systemCore.SystemCore
import com.system32.systemCore.utils.text.TextUtil.Companion.color
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

class ConfigLoader<T : Any>(
    private val clazz: KClass<T>
) {

    private val converters: MutableMap<KClass<*>, ConverterEntry<*>> = mutableMapOf()

    private var configInstance: T
    private val file: File
    private val plugin: JavaPlugin

    init {
        val configAnnotation = clazz.findAnnotation<Config>()
            ?: error("Class ${clazz.simpleName} must be annotated with @Config")
        val ignorePathsAnnotation = clazz.findAnnotation<IgnorePaths>()
        val ignoredPaths = ignorePathsAnnotation?.paths?.toList() ?: emptyList()

        plugin = SystemCore.plugin
        val folder = plugin.dataFolder
        if (!folder.exists()) {
            folder.mkdirs()
        }
        file = File(plugin.dataFolder, configAnnotation.path+".yml")

        if (!file.exists()) {
            file.createNewFile()
        }

        registerDefaultConverters()

        val defaultInstance = createDefaultInstance()
        val defaultMap = instanceToMap(defaultInstance)
        println("Default map: $defaultMap")

        val yaml = Yaml()
        val loadedMap = yaml.load<Map<String, Any>>(FileReader(file))?.toMutableMap() ?: mutableMapOf()

        val mergedMap = mergeMaps(loadedMap, defaultMap)
        println("Merged map: $mergedMap")

        file.bufferedWriter().use { writer ->
            yaml.dump(mergedMap, writer)
        }

        configInstance = buildFromMap(clazz, mergedMap)
    }

    private fun createDefaultInstance(): T {
        val constructor = clazz.primaryConstructor ?: error("No primary constructor for ${clazz.simpleName}")
        val args = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            if (!param.isOptional && !param.type.isMarkedNullable) {
                val type = param.type.classifier as? KClass<*>
                args[param] = when (type) {
                    Boolean::class -> false
                    Int::class -> 0
                    Double::class -> 0.0
                    String::class -> "write something here"
                    List::class -> emptyList<Any>()
                    else -> {
                        val converterEntry = converters[type]
                        if (converterEntry != null) {
                            converterEntry.converter(converterEntry.defaultRaw)
                        } else null
                    }
                }
            }
        }
        return constructor.callBy(args)
    }



    fun get(): T = configInstance

    fun reload(): T {
        configInstance = load()
        return configInstance
    }

    private fun load(): T {
        val yaml = Yaml()
        val map = yaml.load<Map<String, Any>>(FileReader(file))
        return buildFromMap(clazz, map)
    }

    private fun <T : Any> buildFromMap(clazz: KClass<T>, map: Map<String, Any>): T {
        val constructor = clazz.primaryConstructor ?: error("No primary constructor for ${clazz.simpleName}")
        val args = mutableMapOf<KParameter, Any?>()

        for (param in constructor.parameters) {
            val name = param.name ?: continue
            val kebabName = name.camelToKebabCase()

            val property = clazz.memberProperties.find { it.name == name }
            if (property?.findAnnotation<IgnorePaths>() != null) continue

            val value = map[kebabName] ?: continue
            val type = param.type.classifier as? KClass<*>

            val finalValue = when {
                type == null -> value
                converters.containsKey(type) -> converters[type]!!.converter(value)
                type.isData && value is Map<*, *> -> buildFromMap(type, value as Map<String, Any>)
                type.isSubclassOf(List::class) && value is List<*> -> value
                else -> value
            }

            args[param] = finalValue
        }

        return constructor.callBy(args)
    }


    private fun <T : Any> instanceToMap(instance: T): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val clazz = instance::class
        clazz.memberProperties.filterIsInstance<KProperty1<T, Any>>().forEach { prop ->
            val value = prop.get(instance)
            val key = prop.name.camelToKebabCase()
            when {
                value::class.isData -> result[key] = instanceToMap(value)
                value is List<*> -> {
                    val list = value.map {
                        if (it != null && it::class.isData) instanceToMap(it) else it
                    }
                    result[key] = list
                }
                else -> result[key] = value
            }
        }
        return result
    }


    @Suppress("UNCHECKED_CAST")
    private fun mergeMaps(base: MutableMap<String, Any>, defaults: Map<String, Any>): MutableMap<String, Any> {
        for ((key, defaultValue) in defaults) {
            val baseValue = base[key]
            when {
                baseValue == null -> base[key] = defaultValue
                baseValue is Map<*, *> && defaultValue is Map<*, *> -> {
                    val baseMap = baseValue as MutableMap<String, Any>
                    val defaultMap = defaultValue as Map<String, Any>
                    base[key] = mergeMaps(baseMap, defaultMap)
                }
            }
        }
        return base
    }

    fun <C : Any> registerConverter(type: KClass<C>, converter: (Any) -> C, defaultRaw: Any) {
        converters[type] = ConverterEntry(converter, defaultRaw)
    }

    private fun registerDefaultConverters() {
        registerConverter(Component::class,{ raw ->
            val input = raw as String
            color(input)
        }, "hello")

        registerConverter(ItemStack::class, { raw ->
            val map = raw as Map<*, *>
            val materialName = map["material"] as? String
                ?: error("Material missing in ItemBuilder config")
            val material = Material.valueOf(materialName)
            if (material == Material.AIR) {
                error("Invalid material name: $materialName")
            }

            val nameRaw = map["name"] as? String
                ?: error("Name missing in ItemBuilder config")
            val name = color(nameRaw)

            val loreRaw = map["lore"] as? List<*>
                ?: emptyList<Any>()
            val lore = loreRaw.filterIsInstance<String>()
                .map { color(it) }

            val model = map["model"] as? Int
            val amount = map["amount"] as? Int ?: 1

            val item = ItemStack(material)
            item.amount = amount
            item.editMeta { meta ->
                meta.displayName(name)
                meta.lore(lore)
                if (model != null) {
                    meta.setCustomModelData(model)
                }
            }

            item
        }, mapOf(
            "material" to "STONE",
            "name" to "&7Stone",
            "lore" to emptyList<String>(),
            "model" to null,
            "amount" to 1
        ))
    }

    private fun String.camelToKebabCase(): String {
        return replace(Regex("([a-z])([A-Z]+)"), "$1-$2").lowercase()
    }

}

data class ConverterEntry<T : Any>(
    val converter: (Any) -> T,
    val defaultRaw: Any
)

