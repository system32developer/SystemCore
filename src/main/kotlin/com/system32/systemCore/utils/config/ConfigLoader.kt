package com.system32.systemCore.utils.config

import com.system32.systemCore.SystemCore
import com.system32.systemCore.utils.text.TextUtil.Companion.asText
import com.system32.systemCore.utils.text.TextUtil.Companion.color
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
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

    private val constructor: KFunction<T>
    private val parameters: List<KParameter>
    private val paramToProperty: Map<KParameter, KProperty1<T, *>?>

    private val yaml: Yaml
    private val dumperOptions: DumperOptions

    init {
        val configAnnotation = clazz.findAnnotation<Config>()
            ?: error("Class ${clazz.simpleName} must be annotated with @Config")

        plugin = SystemCore.plugin
        val folder = plugin.dataFolder
        if (!folder.exists()) folder.mkdirs()

        file = File(plugin.dataFolder, configAnnotation.path + ".yml")
        if (!file.exists()) file.createNewFile()

        registerDefaultConverters()

        constructor = clazz.primaryConstructor ?: error("No primary constructor for ${clazz.simpleName}")
        parameters = constructor.parameters.toList()
        @Suppress("UNCHECKED_CAST")
        paramToProperty = parameters.associateWith { param ->
            clazz.memberProperties.find { it.name == param.name } as? KProperty1<T, *>
        }

        dumperOptions = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
        }
        yaml = Yaml(dumperOptions)

        configInstance = createDefaultInstance()
    }

    fun load(): T {
        val defaultMap = instanceToMap(configInstance)
        val loadedMap = yaml.load<Map<String, Any>>(FileReader(file))?.toMutableMap() ?: mutableMapOf()
        val mergedMap = mergeMaps(loadedMap, defaultMap)
        val normalizedMap = normalizeForYaml(mergedMap)
        file.bufferedWriter().use { writer -> yaml.dump(normalizedMap, writer) }
        configInstance = buildFromMap(mergedMap)
        return configInstance
    }


    private fun createDefaultInstance(): T {
        val args = mutableMapOf<KParameter, Any?>()
        for (param in parameters) {
            if (!param.isOptional && !param.type.isMarkedNullable) {
                val type = param.type.classifier as? KClass<*>
                args[param] = when {
                    type == Boolean::class -> false
                    type == Int::class -> 0
                    type == Double::class -> 0.0
                    type == String::class -> "write something here"
                    type == List::class -> emptyList<Any>()
                    type != null && type.isData -> createDefaultInstance(type)
                    else -> null
                }
            }
        }
        return constructor.callBy(args)
    }

    private fun <R : Any> createDefaultInstance(type: KClass<R>): R {
        val cons = type.primaryConstructor ?: error("No primary constructor for ${type.simpleName}")
        val params = cons.parameters
        val args = mutableMapOf<KParameter, Any?>()
        for (param in params) {
            if (!param.isOptional && !param.type.isMarkedNullable) {
                val paramType = param.type.classifier as? KClass<*>
                args[param] = when {
                    paramType == Boolean::class -> false
                    paramType == Int::class -> 0
                    paramType == Double::class -> 0.0
                    paramType == String::class -> "write something here"
                    paramType == List::class -> emptyList<Any>()
                    paramType != null && paramType.isData -> createDefaultInstance(paramType)
                    else -> null
                }
            }
        }
        return cons.callBy(args)
    }

    fun get(): T = configInstance

    fun reload(): T {
        val loadedMap = yaml.load<Map<String, Any>>(FileReader(file)) ?: error("Config file is empty or malformed")
        configInstance = buildFromMap(loadedMap)
        return configInstance
    }

    private fun buildFromMap(map: Map<String, Any>): T {
        val args = mutableMapOf<KParameter, Any?>()

        for (param in parameters) {
            val name = param.name ?: continue
            val kebabName = name.camelToKebabCase()

            val property = paramToProperty[param]
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

    private fun <R : Any> buildFromMap(clazz: KClass<R>, map: Map<String, Any>): R {
        val cons = clazz.primaryConstructor ?: error("No primary constructor for ${clazz.simpleName}")
        val params = cons.parameters
        val args = mutableMapOf<KParameter, Any?>()

        for (param in params) {
            val name = param.name ?: continue
            val kebabName = name.camelToKebabCase()
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

        return cons.callBy(args)
    }

    private fun <T : Any> instanceToMap(instance: T): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val clazz = instance::class
        clazz.memberProperties.filterIsInstance<KProperty1<T, Any?>>().forEach { prop ->
            val value = prop.get(instance) ?: return@forEach
            val key = prop.name.camelToKebabCase()
            val type = value::class

            when {
                converters.containsKey(type) -> {
                    val converterEntry = converters[type] as ConverterEntry<Any>
                    result[key] = converterEntry.toConfig(value)
                }
                type.isData -> result[key] = instanceToMap(value)
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

    fun <C : Any> registerConverter(
        type: KClass<C>,
        converter: (Any) -> C,
        toConfig: (C) -> Any
    ) {
        converters[type] = ConverterEntry(converter, toConfig,)
    }

    private fun normalizeForYaml(map: Map<String, Any?>): Map<String, Any?> {
        return map.mapValues { (_, value) ->
            when {
                value == null -> null
                converters.containsKey(value::class) -> {
                    val converterEntry = converters[value::class] as ConverterEntry<Any>
                    converterEntry.toConfig(value)
                }
                value is Map<*, *> -> normalizeForYaml(value as Map<String, Any?>)
                value is List<*> -> value.map { item ->
                    if (item != null && converters.containsKey(item::class)) {
                        val converterEntry = converters[item::class] as ConverterEntry<Any>
                        converterEntry.toConfig(item)
                    } else if (item is Map<*, *>) {
                        normalizeForYaml(item as Map<String, Any?>)
                    } else item
                }
                else -> value
            }
        }
    }


    private fun registerDefaultConverters() {
        registerConverter(Component::class,
            converter = { raw -> color(raw as String) },
            toConfig = { component -> asText(component) }
        )

        registerConverter(ItemStack::class,
            converter = { raw ->
                val map = raw as Map<*, *>
                val materialName = map["material"] as? String ?: error("Material missing in ItemBuilder config")
                val material = Material.valueOf(materialName)
                if (material == Material.AIR) error("Invalid material name: $materialName")

                val nameRaw = map["name"] as? String ?: error("Name missing in ItemBuilder config")
                val name = color(nameRaw)

                val loreRaw = map["lore"] as? List<*> ?: emptyList<Any>()
                val lore = loreRaw.filterIsInstance<String>().map { color(it) }

                val model = map["model"] as? Int
                val amount = map["amount"] as? Int ?: 1

                val item = ItemStack(material)
                item.amount = amount
                item.editMeta { meta ->
                    meta.displayName(name)
                    meta.lore(lore)
                    if (model != null) meta.setCustomModelData(model)
                }

                item
            },
            toConfig = { itemStack ->
                val meta = itemStack.itemMeta!!
                mapOf(
                    "material" to itemStack.type.name,
                    "name" to asText(meta.displayName()!!),
                    "lore" to (meta.lore()?.map { asText(it) } ?: emptyList()),
                    "model" to if (meta.hasCustomModelData()) meta.customModelData else 0,
                    "amount" to itemStack.amount
                )
            }
        )
    }

    private fun String.camelToKebabCase(): String {
        return replace(Regex("([a-z])([A-Z]+)"), "$1-$2").lowercase()
    }
}
data class ConverterEntry<T : Any>(
    val converter: (Any) -> T,
    val toConfig: (T) -> Any,
)
