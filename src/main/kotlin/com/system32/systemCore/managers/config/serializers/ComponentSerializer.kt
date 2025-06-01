package com.system32.systemCore.managers.config.serializers

import com.system32.systemCore.utils.text.TextUtil
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

class ComponentSerializer : TypeSerializer<Component> {

    override fun deserialize(type: Type?, node: ConfigurationNode?): Component? {
        if (node == null || node.string == null) return null
        val raw = node.string!!
        return TextUtil.color(raw)
    }

    override fun serialize(type: Type?, obj: Component?, node: ConfigurationNode?) {
        if (node == null) return
        if (obj == null) {
            node.set(null)
            println("No Component to save, setting node to null")
            return
        }

        val str = TextUtil.asText(obj)
        node.set(str)
    }
}