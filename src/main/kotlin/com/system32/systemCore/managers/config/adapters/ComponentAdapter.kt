package com.system32.systemCore.managers.config.adapters

import com.system32.systemCore.managers.config.ConfigAdapter
import com.system32.systemCore.utils.text.TextUtil.asText
import com.system32.systemCore.utils.text.TextUtil.color
import net.kyori.adventure.text.Component

class ComponentAdapter : ConfigAdapter<Component> {
    override fun serialize(value: Component): Any {
        return asText(value)
    }

    override fun deserialize(value: Any): Component {
        return color(value.toString())
    }
}