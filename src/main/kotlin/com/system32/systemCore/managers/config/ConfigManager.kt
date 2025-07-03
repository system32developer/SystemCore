package com.system32.systemCore.managers.config

import com.google.common.reflect.TypeToken
import com.system32.systemCore.SystemCore
import com.system32.systemCore.managers.config.serializers.ComponentSerializer
import com.system32.systemCore.managers.config.serializers.LocationSerializer
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

class ConfigManager {

    private val plugin = SystemCore.plugin
    private val configs = mutableMapOf<String, ConfigHolder<out Any>>()
    private val serializers = TypeSerializerCollection.builder()

    init {
        registerDefaultSerializers()
    }

    private fun registerDefaultSerializers() {

        serializer(Location::class.java, LocationSerializer())
        serializer(Component::class.java,ComponentSerializer())
    }

    fun <T : Any> config(
        name: String,
        clazz: Class<T>,
        defaultInstance: T
    ): ConfigManager {
        configs[name] = ConfigHolder(name, clazz, defaultInstance)
        return this
    }

    fun <T : Any> serializer(type: Class<T>, serializer: TypeSerializer<T>): ConfigManager {
        serializers.register(type, serializer)
        return this
    }

    fun build() {
        configs.forEach { (_, holder) ->
            val file = File(plugin.dataFolder, "${holder.name}.yml")
            if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
            if(!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: Exception) {
                    plugin.logger.severe("Error creating config file '${holder.name}': ${e.message}")
                    e.printStackTrace()
                }
            }

            val loader = YamlConfigurationLoader.builder()
                .file(file)
                .defaultOptions { opts ->
                    opts.serializers { it.registerAll(serializers.build()) }.implicitInitialization(true)
                }
                .nodeStyle(NodeStyle.BLOCK)
                .build()


            val node = try {
                loader.load()
            } catch (e: Exception) {
                plugin.logger.severe("Error loading config '${holder.name}': ${e.message}")
                e.printStackTrace()
                loader.createNode()
            }

            val loaded = try {
                node.get(holder.clazz) ?: holder.default
            } catch (e: Exception) {
                plugin.logger.severe("Error parsing config '${holder.name}': ${e.message}")
                e.printStackTrace()
                holder.default
            }

            node.set(holder.clazz, loaded)
            loader.save(node)

            @Suppress("UNCHECKED_CAST")
            val typedHolder = holder as ConfigHolder<Any>
            typedHolder.apply {
                this.loader = loader
                this.node = node
                this.instance = loaded
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(name: String): T? {
        return configs[name]?.instance as? T
    }

    fun remove(name: String) {
        configs.remove(name)
    }

    fun save(name: String) {
        val holder = configs[name] ?: return
        try {
            holder.node?.set(holder.clazz, holder.instance)
            holder.loader?.save(holder.node)
        } catch (e: Exception) {
            plugin.logger.severe("Error saving config '$name': ${e.message}")
            e.printStackTrace()
        }
    }

    fun reload(name: String) {
        val holder = configs[name] ?: return
        try {
            @Suppress("UNCHECKED_CAST")
            val typedHolder = holder as ConfigHolder<Any>
            val node = typedHolder.loader?.load() ?: return
            val loaded = node.get(typedHolder.clazz) ?: typedHolder.default
            typedHolder.node = node
            typedHolder.instance = loaded

        } catch (e: Exception) {
            plugin.logger.severe("Error reloading config '$name': ${e.message}")
            e.printStackTrace()
        }
    }

    fun saveAll() {
        configs.keys.forEach { save(it) }
    }

    fun reloadAll() {
        configs.keys.forEach { reload(it) }
    }


    private class ConfigHolder<T : Any>(
        val name: String,
        val clazz: Class<T>,
        val default: T,
        var loader: YamlConfigurationLoader? = null,
        var node: ConfigurationNode? = null,
        var instance: T? = null
    )
}
