package com.system32dev.systemCore.managers.config

import com.system32dev.systemCore.SystemCore
import com.system32dev.systemCore.managers.config.serializers.ConfigItem
import com.system32dev.systemCore.managers.config.serializers.Feedback
import com.system32dev.systemCore.managers.config.serializers.LocationSerializer
import com.system32dev.systemCore.managers.config.serializers.RemoteConnectionData
import org.bukkit.Location
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

class ConfigManager(
    private val baseFolder: File = SystemCore.plugin.dataFolder
) {

    private val plugin = SystemCore.plugin
    private val configs = mutableMapOf<String, ConfigHolder<out Any>>()
    private val serializers = TypeSerializerCollection.builder()

    init {
        registerDefaultSerializers()
    }

    private fun registerDefaultSerializers() {
        serializer(Location::class.java to LocationSerializer())
        serializer(Feedback::class.java to Feedback.Serializer())
        serializer(RemoteConnectionData::class.java to RemoteConnectionData.Serializer())
        serializer(ConfigItem::class.java to ConfigItem.Serializer())
    }

    fun <T : Any> config(
        name: String,
        clazz: Pair<Class<T>, T>
    ): ConfigManager {
        configs[name] = ConfigHolder(name, clazz.first, clazz.second)
        return this
    }

    /**
     * Registers a custom serializer for a specific type.
     * @param type The class type to register the serializer for.
     * @param serializer The serializer instance to handle the specified type.
     * @return The current instance of ConfigManager for method chaining.
     *
     * Example:
     * ```kotlin
     * configManager.serializer(CustomType::class.java, CustomTypeSerializer())
     * ```
     *
     * Serializer Example:
     * ```kotlin
     * class CustomTypeSerializer : TypeSerializer<CustomType> {
     *     override fun deserialize(type: Type?, node: ConfigurationNode?): CustomType? {
     *         // Implement deserialization logic
     *     }
     *     override fun serialize(type: Type?, obj: CustomType?, node: ConfigurationNode?) {
     *     // Implement serialization logic
     *     }
     * }
     * ```
     */

    fun <T : Any> serializer(serializer: Pair<Class<T>, TypeSerializer<T>>): ConfigManager {
        serializers.register(serializer.first, serializer.second)
        return this
    }

    fun build() {
        configs.forEach { (_, holder) ->
            val file = File(baseFolder, "${holder.name}.yml")
            if (!baseFolder.exists()) baseFolder.mkdirs()
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