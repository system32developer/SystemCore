package com.system32.systemCore.utils.config.configurate.adapters

import com.system32.systemCore.utils.config.configurate.ConfigAdapter
import com.system32.systemCore.utils.text.TextUtil.asText
import com.system32.systemCore.utils.text.TextUtil.color
import net.kyori.adventure.text.Component

class ComponentListAdapter : ConfigAdapter<List<Component>> {
    override fun serialize(value: List<Component>): Any {
        return value.map { asText(it) }
            .ifEmpty { listOf("") }
    }

    override fun deserialize(value: Any): List<Component> {
        if (value !is List<*>) {
            error("Expected List<String> for Component list, but got: ${value::class.simpleName}")
        }

        return value.mapNotNull {
            when (it) {
                is String -> color(it)
                else -> null
            }
        }
    }
}