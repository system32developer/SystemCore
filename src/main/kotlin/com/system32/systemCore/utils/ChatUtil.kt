package com.system32.systemCore.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

import java.util.function.Consumer

class ChatUtil {
    companion object{
        fun asComponent(message: String): Component {
            val legacy: TextComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
            val finalmessage: String = MiniMessage.miniMessage().serialize(legacy).replace("\\", "");
            return if (finalmessage.isEmpty()) Component.empty() else MiniMessage.miniMessage().deserialize(
                "<!italic>$finalmessage"
            )
        }

        fun asComponents(messages: List<String>): List<Component> {
            val components: MutableList<Component> = ArrayList()
            messages.forEach(Consumer { message: String -> components.add(asComponent(message)) })
            return components
        }

        fun asText(component: Component): String {
            return MiniMessage.miniMessage().serialize(component)
        }
    }
}