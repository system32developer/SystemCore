package com.system32.systemCore.utils.config

import com.google.common.base.Preconditions
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.file.YamlConstructor
import org.bukkit.configuration.file.YamlRepresenter
import org.bukkit.plugin.Plugin
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.function.Consumer


object ConfigUpdater {
    private const val SEPARATOR = '.'
    private val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8

    @Throws(IOException::class)
    fun update(plugin: Plugin, resourceName: String, toUpdate: File, vararg ignoredSections: String?) {
        update(plugin, resourceName, toUpdate, Arrays.asList(*ignoredSections))
    }

    @Throws(IOException::class)
    fun update(plugin: Plugin, resourceName: String, toUpdate: File, ignoredSections: List<String?>?) {
        Preconditions.checkArgument(toUpdate.exists(), "The toUpdate file doesn't exist!")

        val defaultConfig: FileConfiguration =
            YamlConfiguration.loadConfiguration(InputStreamReader(plugin.getResource(resourceName), DEFAULT_CHARSET))
        val currentConfig: FileConfiguration =
            YamlConfiguration.loadConfiguration(Files.newBufferedReader(toUpdate.toPath(), DEFAULT_CHARSET))
        val comments = parseComments(plugin, resourceName, defaultConfig)
        val ignoredSectionsValues = parseIgnoredSections(
            toUpdate, comments,
            (ignoredSections ?: emptyList()).filterNotNull()
        )
        val writer = StringWriter()
        write(defaultConfig, currentConfig, BufferedWriter(writer), comments, ignoredSectionsValues)
        val value = writer.toString()

        val toUpdatePath = toUpdate.toPath()
        if (value != String(Files.readAllBytes(toUpdatePath), DEFAULT_CHARSET)) {
            Files.write(toUpdatePath, value.toByteArray(DEFAULT_CHARSET))
        }
    }

    @Throws(IOException::class)
    private fun write(
        defaultConfig: FileConfiguration,
        currentConfig: FileConfiguration,
        writer: BufferedWriter,
        comments: Map<String?, String>,
        ignoredSectionsValues: Map<String, String>
    ) {
        val yaml = yamlWriter

        for (fullKey in defaultConfig.getKeys(true)) {
            val indents = KeyUtils.getIndents(fullKey, SEPARATOR)


            if (!ignoredSectionsValues.isEmpty()) {
                if (writeIgnoredSectionValueIfExists(ignoredSectionsValues, writer, fullKey)) continue
            }
            writeCommentIfExists(comments, writer, fullKey, indents)
            var currentValue = currentConfig[fullKey]

            if (currentValue == null) currentValue = defaultConfig[fullKey]

            val splitFullKey =
                fullKey.split(("[" + SEPARATOR + "]").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val trailingKey = splitFullKey[splitFullKey.size - 1]

            if (currentValue is ConfigurationSection) {
                writeConfigurationSection(writer, indents, trailingKey, currentValue)
                continue
            }
            writeYamlValue(yaml, writer, indents, trailingKey, currentValue)
        }

        val danglingComments = comments[null]

        if (danglingComments != null) writer.write(danglingComments)

        writer.close()
    }

    @Throws(IOException::class)
    private fun parseComments(
        plugin: Plugin,
        resourceName: String,
        defaultConfig: FileConfiguration
    ): Map<String?, String> {
        val keys: List<String> = ArrayList(defaultConfig.getKeys(true))
        val reader = BufferedReader(InputStreamReader(plugin.getResource(resourceName), DEFAULT_CHARSET))
        val comments: MutableMap<String?, String> = LinkedHashMap()
        val commentBuilder = StringBuilder()
        val keyBuilder = KeyBuilder(defaultConfig, SEPARATOR)
        var currentValidKey: String? = null

        var line: String
        while ((reader.readLine().also { line = it }) != null) {
            val trimmedLine = line.trim { it <= ' ' }
            if (trimmedLine.startsWith("-")) continue

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                commentBuilder.append(trimmedLine).append("\n")
            } else {
                if (!line.startsWith(" ")) {
                    keyBuilder.clear()
                    currentValidKey = trimmedLine
                }

                keyBuilder.parseLine(trimmedLine, true)
                val key = keyBuilder.toString()

                if (commentBuilder.length > 0) {
                    comments[key] = commentBuilder.toString()
                    commentBuilder.setLength(0)
                }

                val nextKeyIndex = keys.indexOf(keyBuilder.toString()) + 1
                if (nextKeyIndex < keys.size) {
                    val nextKey = keys[nextKeyIndex]
                    while (!keyBuilder.isEmpty() && !nextKey.startsWith(keyBuilder.toString())) {
                        keyBuilder.removeLastKey()
                    }
                    if (keyBuilder.isEmpty()) {
                        keyBuilder.parseLine(currentValidKey!!, false)
                    }
                }
            }
        }
        reader.close()

        if (commentBuilder.length > 0) comments[null] = commentBuilder.toString()

        return comments
    }

    @Throws(IOException::class)
    private fun parseIgnoredSections(
        toUpdate: File,
        comments: Map<String?, String>,
        ignoredSections: List<String>
    ): Map<String, String> {
        val ignoredSectionValues: MutableMap<String, String> = LinkedHashMap(ignoredSections.size)

        val options = DumperOptions()
        options.lineBreak = DumperOptions.LineBreak.UNIX
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val yaml = Yaml(YamlConstructor(), YamlRepresenter(), options)

        val root = yaml.load<Any>(InputStreamReader(FileInputStream(toUpdate), DEFAULT_CHARSET)) as Map<Any?, Any>
        ignoredSections.forEach(Consumer { section: String ->
            val split = section.split(("[" + SEPARATOR + "]").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val key = split[split.size - 1]
            val map = getSection(section, root)

            val keyBuilder = StringBuilder()
            for (i in split.indices) {
                if (i != split.size - 1) {
                    if (keyBuilder.length > 0) keyBuilder.append(SEPARATOR)

                    keyBuilder.append(split[i])
                }
            }
            ignoredSectionValues[section] =
                buildIgnored(key, map, comments, keyBuilder, StringBuilder(), yaml)
        })
        return ignoredSectionValues
    }

    private fun getSection(fullKey: String, root: Map<Any?, Any>): Map<Any?, Any> {
        val keys = fullKey.split(("[" + SEPARATOR + "]").toRegex(), limit = 2).toTypedArray()
        val key = keys[0]
        val value = root[getKeyAsObject(key, root)]

        if (keys.size == 1) {
            if (value is Map<*, *>) return root
            throw IllegalArgumentException("Ignored sections must be a ConfigurationSection not a value!")
        }

        require(value is Map<*, *>) { "Invalid ignored ConfigurationSection specified!" }

        return getSection(keys[1], value as Map<Any?, Any>)
    }

    private fun buildIgnored(
        fullKey: String,
        ymlMap: Map<Any?, Any>,
        comments: Map<String?, String>,
        keyBuilder: StringBuilder,
        ignoredBuilder: StringBuilder,
        yaml: Yaml
    ): String {
        var keyBuilder = keyBuilder
        val keys = fullKey.split(("[" + SEPARATOR + "]").toRegex(), limit = 2).toTypedArray()
        val key = keys[0]
        val originalKey = getKeyAsObject(key, ymlMap)

        if (keyBuilder.length > 0) keyBuilder.append(".")

        keyBuilder.append(key)

        if (!ymlMap.containsKey(originalKey)) {
            require(keys.size != 1) { "Invalid ignored section: $keyBuilder" }

            throw IllegalArgumentException("Invalid ignored section: " + keyBuilder + "." + keys[1])
        }

        val comment = comments[keyBuilder.toString()]
        val indents = KeyUtils.getIndents(keyBuilder.toString(), SEPARATOR)

        if (comment != null) ignoredBuilder.append(addIndentation(comment, indents)).append("\n")

        ignoredBuilder.append(addIndentation(key, indents)).append(":")
        val obj = ymlMap[originalKey]

        if (obj is Map<*, *>) {
            val map = obj as Map<Any?, Any>

            if (map.isEmpty()) {
                ignoredBuilder.append(" {}\n")
            } else {
                ignoredBuilder.append("\n")
            }

            val preLoopKey = StringBuilder(keyBuilder)

            for (o in map.keys) {
                buildIgnored(o.toString(), map, comments, keyBuilder, ignoredBuilder, yaml)
                keyBuilder = StringBuilder(preLoopKey)
            }
        } else {
            writeIgnoredValue(yaml, obj, ignoredBuilder, indents)
        }

        return ignoredBuilder.toString()
    }

    private fun writeIgnoredValue(yaml: Yaml, toWrite: Any?, ignoredBuilder: StringBuilder, indents: String) {
        val yml = yaml.dump(toWrite)
        if (toWrite is Collection<*>) {
            ignoredBuilder.append("\n").append(addIndentation(yml, indents)).append("\n")
        } else {
            ignoredBuilder.append(" ").append(yml)
        }
    }

    private fun addIndentation(s: String, indents: String): String {
        val builder = StringBuilder()
        val split = s.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (value in split) {
            if (builder.length > 0) builder.append("\n")

            builder.append(indents).append(value)
        }

        return builder.toString()
    }

    @Throws(IOException::class)
    private fun writeCommentIfExists(
        comments: Map<String?, String>,
        writer: BufferedWriter,
        fullKey: String,
        indents: String
    ) {
        val comment = comments[fullKey]

        if (comment != null) writer.write(
            indents + comment.substring(0, comment.length - 1).replace(
                "\n", """
     
     $indents
     """.trimIndent()
            ) + "\n"
        )
    }

    private fun getKeyAsObject(key: String, sectionContext: Map<Any?, Any>): Any? {
        if (sectionContext.containsKey(key)) return key

        try {
            val keyFloat = key.toFloat()

            if (sectionContext.containsKey(keyFloat)) return keyFloat
        } catch (ignored: NumberFormatException) {
        }

        try {
            val keyDouble = key.toDouble()

            if (sectionContext.containsKey(keyDouble)) return keyDouble
        } catch (ignored: NumberFormatException) {
        }

        try {
            val keyInteger = key.toInt()

            if (sectionContext.containsKey(keyInteger)) return keyInteger
        } catch (ignored: NumberFormatException) {
        }

        try {
            val longKey = key.toLong()

            if (sectionContext.containsKey(longKey)) return longKey
        } catch (ignored: NumberFormatException) {
        }

        return null
    }

    @Throws(IOException::class)
    private fun writeYamlValue(
        yamlWriter: Yaml,
        bufferedWriter: BufferedWriter,
        indents: String,
        trailingKey: String,
        currentValue: Any?
    ) {
        val map = Collections.singletonMap(trailingKey, currentValue)
        var yaml = yamlWriter.dump(map)
        yaml = yaml.substring(0, yaml.length - 1).replace(
            "\n", """
     
     $indents
     """.trimIndent()
        )
        val toWrite = indents + yaml + "\n"
        bufferedWriter.write(toWrite)
    }

    @Throws(IOException::class)
    private fun writeIgnoredSectionValueIfExists(
        ignoredSectionsValues: Map<String, String>,
        bufferedWriter: BufferedWriter,
        fullKey: String
    ): Boolean {
        val ignored = ignoredSectionsValues[fullKey]
        if (ignored != null) {
            bufferedWriter.write(ignored)
            return true
        }
        for ((key) in ignoredSectionsValues) {
            if (KeyUtils.isSubKeyOf(key, fullKey, SEPARATOR)) {
                return true
            }
        }
        return false
    }

    @Throws(IOException::class)
    private fun writeConfigurationSection(
        bufferedWriter: BufferedWriter,
        indents: String,
        trailingKey: String,
        configurationSection: ConfigurationSection
    ) {
        bufferedWriter.write("$indents$trailingKey:")
        if (!(configurationSection).getKeys(false).isEmpty()) {
            bufferedWriter.write("\n")
        } else {
            bufferedWriter.write(" {}\n")
        }
    }

    private val yamlWriter: Yaml
        get() {
            val dumperOptions = DumperOptions()
            dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            dumperOptions.isAllowUnicode = true

            return Yaml(dumperOptions)
        }
}