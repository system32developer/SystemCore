package com.system32.systemCore.utils.config

object KeyUtils {
    /**
     * Checks if the subKey is a sub path of the parentKey.
     *
     * @param parentKey the parent key to check against.
     * @param subKey the part of the key to check if it is a sub path of the parent key.
     * @param separator the separator between each part of the key. The default is a dot.
     * @return true if the subKey is a sub path of the parentKey; returns false if the parentKey is empty or does not contain the subKey.
     */
    fun isSubKeyOf(parentKey: String, subKey: String, separator: Char): Boolean {
        if (parentKey.isEmpty()) return false

        return subKey.startsWith(parentKey)
                && subKey.substring(parentKey.length).startsWith(separator.toString())
    }

    /**
     * Gets the amount of indentation spaces for the provided key.
     *
     * @param key the key to check for the amount of indentation spaces.
     * @param separator the separator used in the nested path. The default separator is a dot.
     * @return a string contains only the amount of indentation spaces to add.
     */
    fun getIndents(key: String, separator: Char): String {
        val splitKey = key.split(("[$separator]").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val builder = StringBuilder()

        for (i in 1..<splitKey.size) {
            builder.append("  ")
        }
        return builder.toString()
    }
}