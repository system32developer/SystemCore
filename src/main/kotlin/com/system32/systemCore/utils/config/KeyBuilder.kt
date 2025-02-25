package com.system32.systemCore.utils.config

import org.bukkit.configuration.file.FileConfiguration
import kotlin.math.max


class KeyBuilder : Cloneable {
    private val config: FileConfiguration
    private val separator: Char
    private val builder: StringBuilder

    /**
     * Constructs a new KeyBuilder instance.
     *
     * @param config the FileConfiguration to work with.
     * @param separator the character used as the separator between key parts. The default separator is a dot ('.').
     */
    constructor(config: FileConfiguration, separator: Char) {
        this.config = config
        this.separator = separator
        this.builder = StringBuilder()
    }

    /**
     * Constructs a new KeyBuilder instance as a clone of another KeyBuilder.
     *
     * @param keyBuilder the KeyBuilder to clone.
     */
    private constructor(keyBuilder: KeyBuilder) {
        this.config = keyBuilder.config
        this.separator = keyBuilder.separator
        this.builder = StringBuilder(keyBuilder.toString())
    }

    /**
     * Parses the line to check if it represents a valid YAML path. If the
     * config does not contain the path, it removes the last part of the line
     * until it finds a valid path or becomes empty.
     *
     * @param line the line to check if it belongs to the current path set in the [.builder].
     * @param checkIfExists set to true to check if the path is valid in the config.
     */
    fun parseLine(line: String, checkIfExists: Boolean) {
        var line = line
        line = line.trim { it <= ' ' }

        var currentSplitLine = line.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (currentSplitLine.size > 2) currentSplitLine =
            line.split(": ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val key = currentSplitLine[0].replace("'", "").replace("\"", "")

        if (checkIfExists) {
            //Checks keyBuilder path against config to see if the path is valid.
            //If the path doesn't exist in the config it keeps removing last key in keyBuilder.
            while (builder.length > 0 && !config.contains(builder.toString() + separator + key)) {
                removeLastKey()
            }
        }

        //Add the separator if there is already a key inside keyBuilder
        //If currentSplitLine[0] is 'key2' and keyBuilder contains 'key1' the result will be 'key1.' if '.' is the separator
        if (builder.length > 0) builder.append(separator)

        //Appends the current key to keyBuilder
        //If keyBuilder is 'key1.' and currentSplitLine[0] is 'key2' the resulting keyBuilder will be 'key1.key2' if separator is '.'
        builder.append(key)
    }

    val lastKey: String
        /**
         * Gets the last key in the builder.
         *
         * @return the last key, or an empty string if the builder is empty.
         */
        get() {
            if (builder.length == 0) return ""

            return builder.toString().split(("[$separator]").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        }

    fun isEmpty(): Boolean {
        return builder.isEmpty()
    }

    fun clear() {
        builder.setLength(0)
    }

    /**
     * Checks if the full key path represented by this instance is a sub-key of the specified parent key.
     *
     * @param parentKey the parent key to check against.
     * @return true if the full key path is a sub-key of the parentKey; otherwise, false.
     */
    fun isSubKeyOf(parentKey: String?): Boolean {
        return KeyUtils.isSubKeyOf(parentKey!!, builder.toString(), separator)
    }

    /**
     * Checks if the specified subKey is a sub-key of the key path represented by this instance.
     *
     * @param subKey the sub-key to check.
     * @return true if the subKey is a sub-key of the key path; otherwise, false.
     */
    fun isSubKey(subKey: String?): Boolean {
        return KeyUtils.isSubKeyOf(builder.toString(), subKey!!, separator)
    }

    val isConfigSection: Boolean
        /**
         * Checks if the key path represented by this instance is a configuration section in the FileConfiguration.
         *
         * @return true if the key path is a configuration section; otherwise, false.
         */
        get() {
            val key = builder.toString()
            return config.isConfigurationSection(key)
        }

    val isConfigSectionWithKeys: Boolean
        /**
         * Checks if the key path represented by this instance is a non-empty configuration section in the FileConfiguration.
         *
         * @return true if the key path is a non-empty configuration section; otherwise, false.
         */
        get() {
            val key = builder.toString()
            return config.isConfigurationSection(key) && !config.getConfigurationSection(key)!!.getKeys(false).isEmpty()
        }

    /**
     * Removes the last key from the builder.
     *
     * For example, if the input is 'key1.key2', the result will be 'key1'.
     */
    fun removeLastKey() {
        if (builder.length == 0) return

        val keyString = builder.toString()
        //Must be enclosed in brackets in case a regex special character is the separator
        val split = keyString.split(("[$separator]").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        //Makes sure begin index isn't < 0 (error). Occurs when there is only one key in the path
        val minIndex = max(0.0, (builder.length - split[split.size - 1].length - 1).toDouble()).toInt()
        builder.replace(minIndex, builder.length, "")
    }

    /**
     * Returns the current key path as a string.
     *
     * @return the current key path.
     */
    override fun toString(): String {
        return builder.toString()
    }

    /**
     * Creates a clone of this KeyBuilder instance.
     *
     * @return a new KeyBuilder instance with the same contents as this instance.
     */
    override fun clone(): KeyBuilder {
        return KeyBuilder(this)
    }
}