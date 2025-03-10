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
        private const val CENTER_PX: Int = 154
        const val NORMAL_LINE: String = "&7&m-----------------------------"
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

        fun centerMessage(message: String?): String {
            if (message.isNullOrEmpty()) {
                return ""
            }

            var messagePxSize = 0
            var previousCode = false
            var isBold = false

            for (c in message.toCharArray()) {
                when {
                    c == '§' -> {
                        previousCode = true
                        continue
                    }
                    previousCode -> {
                        previousCode = false
                        isBold = (c == 'l' || c == 'L')
                        continue
                    }
                }

                val dFI = FontInfo.getDefaultFontInfo(c)
                messagePxSize += if (isBold) dFI.boldLength else dFI.length
                messagePxSize++
            }

            val halvedMessageSize = messagePxSize / 2
            val toCompensate = CENTER_PX - halvedMessageSize
            val spaceLength = FontInfo.SPACE.length + 1
            var compensated = 0
            val sb = StringBuilder()

            while (compensated < toCompensate) {
                sb.append(" ")
                compensated += spaceLength
            }

            return sb.toString() + message
        }
        fun upperCaseFirst(string: String): String {
            return string.substring(0, 1).uppercase() + string.substring(1).lowercase()
        }
    }
}