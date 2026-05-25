package com.system32dev.systemCore.managers.config

import com.system32dev.systemCore.SystemCore
import com.system32dev.systemCore.managers.config.serializers.ConfigItem
import com.system32dev.systemCore.managers.config.serializers.Feedback
import com.system32dev.systemCore.managers.config.serializers.LocationSerializer
import com.system32dev.systemCore.managers.config.serializers.Range
import com.system32dev.systemCore.managers.config.serializers.RemoteConnectionData
import org.bukkit.Location
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

class ConfigManager(
    @PublishedApi
    internal var baseFolder: File = SystemCore.plugin.dataFolder,
    builder: (ConfigManager.() -> Unit)? = null
) {

    /**
     * This creates a folder inside baseFolder and uses it as a new base
     */
    fun subFolderOf(name: String) {
        val subFolder = File(baseFolder, name)
        subFolder.mkdirs()
        baseFolder = subFolder
    }

    @PublishedApi
    internal val configs = mutableMapOf<String, ConfigHolder<*>>()
    @PublishedApi
    internal val serializers = TypeSerializerCollection.builder()

    init {
        registerDefaultSerializers()
        builder?.invoke(this)
        build()
    }

    private fun registerDefaultSerializers() {
        serializer(Location::class.java to LocationSerializer())
        serializer(Feedback::class.java to Feedback.Serializer())
        serializer(RemoteConnectionData::class.java to RemoteConnectionData.Serializer())
        serializer(ConfigItem::class.java to ConfigItem.Serializer())
        serializer(Range::class.java to Range.Serializer())
    }

    inline fun <reified T : Any> config(
        name: String,
        default: T
    ): ConfigManager {
        configs[name] = ConfigHolder(
            name,
            T::class.java,
            default
        )

        return this
    }

    inline fun <reified T : Any> config(
        name: String
    ): ConfigManager {
        val instance = T::class.java
            .getDeclaredConstructor()
            .newInstance()

        configs[name] = ConfigHolder(
            name,
            T::class.java,
            instance
        )

        return this
    }

    fun <T : Any> configAllExisting(clazz: Pair<Class<T>, T>) {
        baseFolder.listFiles { file ->
            file.isFile && file.extension == "yml"
        }?.forEach { file ->

            val name = file.nameWithoutExtension

            if (!configs.containsKey(name)) {
                configs[name] = ConfigHolder(
                    name,
                    clazz.first,
                    clazz.second
                )
            }
        }
    }

    inline fun <reified T : Any> configAllExisting() {
        val default = T::class.java
            .getDeclaredConstructor()
            .newInstance()

        baseFolder.listFiles { file ->
            file.isFile && file.extension == "yml"
        }?.forEach { file ->

            val name = file.nameWithoutExtension

            if (!configs.containsKey(name)) {
                configs[name] = ConfigHolder(
                    name,
                    T::class.java,
                    default
                )
            }
        }
    }

    fun <T : Any> serializer(
        serializer: Pair<Class<T>, TypeSerializer<T>>
    ): ConfigManager {

        serializers.register(serializer.first, serializer.second)
        return this
    }

    private fun build() {
        configs.forEach { (_, holder) ->

            @Suppress("UNCHECKED_CAST")
            val typedHolder = holder as ConfigHolder<Any>

            val file = File(baseFolder, "${typedHolder.name}.yml")

            if (!baseFolder.exists()) {
                baseFolder.mkdirs()
            }

            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: Exception) {
                    println("Error creating config file '${typedHolder.name}': ${e.message}")
                    e.printStackTrace()
                }
            }

            val loader = YamlConfigurationLoader.builder()
                .file(file)
                .defaultOptions { opts ->
                    opts.serializers {
                        it.registerAll(serializers.build())
                    }.implicitInitialization(true)
                }
                .nodeStyle(NodeStyle.BLOCK)
                .build()

            val node = try {
                loader.load()
            } catch (e: Exception) {
                println("Error loading config '${typedHolder.name}': ${e.message}")
                e.printStackTrace()
                loader.createNode()
            }

            val loaded = try {
                node.get(typedHolder.clazz) ?: typedHolder.default
            } catch (e: Exception) {
                println("Error parsing config '${typedHolder.name}': ${e.message}")
                e.printStackTrace()
                typedHolder.default
            }

            node.set(typedHolder.clazz, loaded)
            loader.save(node)

            typedHolder.loader = loader
            typedHolder.node = node
            typedHolder.instance = loaded
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> get(name: String): T? {
        return configs[name]?.instance as? T
    }

    fun remove(name: String) {
        val holder = configs[name] ?: return

        @Suppress("UNCHECKED_CAST")
        val typedHolder = holder as ConfigHolder<Any>

        try {
            typedHolder.node?.set(
                typedHolder.clazz,
                typedHolder.instance
            )

            typedHolder.loader?.save(typedHolder.node)

        } catch (e: Exception) {
            println("Error saving config '$name' during unload: ${e.message}")
            e.printStackTrace()
        }

        configs.remove(name)
    }

    fun save(name: String) {
        val holder = configs[name] ?: return

        @Suppress("UNCHECKED_CAST")
        val typedHolder = holder as ConfigHolder<Any>

        try {
            typedHolder.node?.set(
                typedHolder.clazz,
                typedHolder.instance
            )

            typedHolder.loader?.save(typedHolder.node)

        } catch (e: Exception) {
            println("Error saving config '$name': ${e.message}")
            e.printStackTrace()
        }
    }

    fun reload(name: String) {
        val holder = configs[name] ?: return

        @Suppress("UNCHECKED_CAST")
        val typedHolder = holder as ConfigHolder<Any>

        try {
            val node = typedHolder.loader?.load() ?: return

            val loaded = node.get(
                typedHolder.clazz
            ) ?: typedHolder.default

            typedHolder.node = node
            typedHolder.instance = loaded

        } catch (e: Exception) {
            println("Error reloading config '$name': ${e.message}")
            e.printStackTrace()
        }
    }

    fun saveAll() {
        configs.keys.forEach(::save)
    }

    fun reloadAll() {
        configs.keys.forEach(::reload)
    }

    @PublishedApi
    internal class ConfigHolder<T : Any>(
        val name: String,
        val clazz: Class<T>,
        val default: T,
        var loader: YamlConfigurationLoader? = null,
        var node: ConfigurationNode? = null,
        var instance: T? = null
    )
}