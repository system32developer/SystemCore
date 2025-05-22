package com.system32.systemCore.utils.config

import com.system32.systemCore.utils.text.TextUtil.Companion.color
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*

class ConfigLoader<T : Any>(
    private val clazz: KClass<T>,
    private val file: File
) {

    private val converters: MutableMap<KClass<*>, (Any) -> Any> = mutableMapOf()
    private var configInstance: T

    init {
        registerDefaultConverters()
        configInstance = load()
    }

    fun getConfig(): T = configInstance

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
            val value = map[name] ?: continue
            val type = param.type.classifier as? KClass<*>

            val finalValue = when {
                type == null -> value
                converters.containsKey(type) -> converters[type]!!.invoke(value)
                type.isData && value is Map<*, *> -> buildFromMap(type, value as Map<String, Any>)
                type.isSubclassOf(List::class) && value is List<*> -> value
                else -> value
            }

            args[param] = finalValue
        }

        return constructor.callBy(args)
    }

    fun <C : Any> registerConverter(type: KClass<C>, converter: (Any) -> C) {
        converters[type] = converter
    }

    private fun registerDefaultConverters() {
        registerConverter(Component::class) { raw ->
            val input = raw as String
            color(input)
        }

        registerConverter(ItemStack::class) { raw ->
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
        }
    }
}