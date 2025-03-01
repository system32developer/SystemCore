package com.system32.systemCore.utils

import com.system32.systemCore.SystemCore
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

import java.util.function.Consumer

class ChatUtil {
    companion object{
        /**
         * Color a string using the MiniMessage API or Bukkit's legacy color codes
         * You can use placeholders using PlaceholderAPI but you need to set it up in your plugin @onEnable first using SystemCore.placeHolderAPIHook(true)
         *
         * @param input The string to color
         * @return The colored string as a Component
         *
         * * ### Example usage:
         * ```
         * val message = ChatUtil.color("&aHello, &bworld!")
         * player.sendMessage(message)
         * ```
         */
        fun color(input: String): Component {
            var message = input
            if(SystemCore.placeHolderAPIHook()){
                message = PlaceholderAPI.setPlaceholders(null, message)
            }
            val legacy: TextComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
            val finalmessage: String = MiniMessage.miniMessage().serialize(legacy).replace("\\", "");
            return if (finalmessage.isEmpty()) Component.empty() else MiniMessage.miniMessage().deserialize(
                "<!italic>$finalmessage"
            )
        }

        fun color(messages: List<String>): List<Component> {
            val components: MutableList<Component> = ArrayList()
            messages.forEach(Consumer { message: String -> components.add(color(message)) })
            return components
        }

        fun asText(component: Component): String {
            return MiniMessage.miniMessage().serialize(component)
        }
    }
}