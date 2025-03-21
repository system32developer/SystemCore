package com.system32.systemCore.utils.discord

class JSONObject {
    private val map = mutableMapOf<String, Any?>()

    fun put(key: String, value: Any?) {
        value?.let { map[key] = it }
    }

    override fun toString(): String {
        return buildString {
            append("{")
            map.entries.joinToString(",") { entry ->
                val value = when (val v = entry.value) {
                    is String -> "\"$v\""
                    is JSONObject -> v.toString()
                    is List<*> -> v.joinToString(",", prefix = "[", postfix = "]")
                    else -> v.toString()
                }
                "\"${entry.key}\":$value"
            }.let { append(it) }
            append("}")
        }
    }
}