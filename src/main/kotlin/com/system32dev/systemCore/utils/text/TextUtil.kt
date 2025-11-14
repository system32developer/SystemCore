package com.system32dev.systemCore.utils.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.util.function.Consumer

object TextUtil {
    private const val CENTER_PX: Int = 154
    const val NORMAL_LINE: String = "&7&m-----------------------------"

    /**
     * Color a string using the MiniMessage API or Bukkit's legacy color codes
     * You can use placeholders using PlaceholderAPI but you need to set it up in your plugin @onEnable first using SystemCore.placeHolderAPIHook(true)
     *
     * @param input The string to color
     * @param tag Optional TagResolver for placeholders.
     * @return The colored string as a Component
     *
     * * ### Example usage:
     * ```
     * val message = ChatUtil.color("<red>Hello, &bworld!")
     * player.sendMessage(message)
     * ```
     */
    fun color(input: String, tag: TagResolver? = null): Component {
        var message = input
        if (message.isEmpty()) return Component.empty()
        val mini = MiniMessage.miniMessage()
        if(input.contains("&")) {
            if(input.contains("§")) message = input.replace("§", "&")
            message = mini.serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(message)).replace("\\", "")
        }
        val component = if(tag != null) mini.deserialize(message, tag) else mini.deserialize(message)
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }


    fun tag(vararg tags: Pair<String, Any>): TagResolver {
        return TagResolver.resolver(
            tags.map { (placeholder, value) ->
                TagResolver.resolver(placeholder, Tag.selfClosingInserting(color(value.toString())))
            }
        )
    }

    /**
     * Colors a list of strings using MiniMessage API or Bukkit's legacy color codes.
     *
     * @param messages List of strings to color.
     * @param tag Optional TagResolver for placeholders.
     * @return A list of colored Components.
     */
    fun color(messages: List<String>, tag: TagResolver? = null): List<Component> {
        val components: MutableList<Component> = ArrayList()
        messages.forEach { components.add(color(it, tag)) }
        return components
    }

    /**
     * Formats a number (Int or String) into a string with thousands separators.
     *
     * @param value The number to format.
     * @return The formatted string representation of the number.
     */

    fun formatNumber(value: Any): String {
        val number = when (value) {
            is Int -> value.toLong()
            is String -> value.toLongOrNull() ?: return value
            else -> throw IllegalArgumentException("The value must be an Int or String.")
        }

        val formatter = DecimalFormat("#,###")
        return formatter.format(number)
    }

    /**
     * Converts a Component to a string representation using MiniMessage serialization.
     *
     * @param component The Component to convert.
     * @return The serialized string representation of the Component.
     */

    fun asText(component: Component): String {
        return MiniMessage.miniMessage().serialize(component)
    }

    /**
     * Converts a Component to a plain text string, removing all formatting.
     *
     * @param component The Component to convert.
     * @return The plain text representation of the Component.
     */

    fun asPlainText(component: Component): String {
        return PlainTextComponentSerializer.plainText().serialize(component)
    }

    /**
     * Centers a message by adding appropriate spacing.
     *
     * @param message The message to center.
     * @return The centered message as a string.
     */

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

    /**
     * Capitalizes the first letter of a string and converts the rest to lowercase.
     *
     * @param string The input string.
     * @return The formatted string with the first letter in uppercase.
     */
    fun caseFirst(string: String): String {
        return string.substring(0, 1).uppercase() + string.substring(1).lowercase()
    }

    /**
     * Converts seconds into a formatted time string (HH:MM:SS).
     *
     * @param seconds The number of seconds to convert.
     * @return A string formatted as HH:MM:SS.
     */
    fun parseTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val seconds = seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    /**
     * Normalizes a number to a range of 0.0 to 1.0 based on a maximum value.
     *
     * @param value The number to normalize.
     * @param max The maximum value for normalization.
     * @return A double value between 0.0 and 1.0.
     */

    fun normalize(value: Number, max: Number): Double {
        return (value.toDouble() / max.toDouble()).coerceIn(0.0, 1.0)
    }

    /**
     * Creates a progress bar string representation.
     *
     * @param value The current value.
     * @param max The maximum value.
     * @param length The length of the progress bar.
     * @param symbol The symbol used for the filled part of the bar.
     * @param filledColor The color for the filled part of the bar.
     * @param emptyColor The color for the empty part of the bar.
     * @return A string representation of the progress bar.
     */

    fun progressBar(value: Double, max: Double, length: Int = 10, symbol: String = "|", filledColor: String = "#e6ff59", emptyColor: String = "#AAAAAA"): String {
        val normalizedValue = normalize(value, max)
        val filledLength = (length * normalizedValue).toInt()
        val emptyLength = length - filledLength
        return "<color:$filledColor>" + symbol.repeat(filledLength) + "<color:$emptyColor>" + symbol.repeat(emptyLength)
    }

    /**
     * Creates a progress bar string representation with integer values.
     *
     * @param value The current value.
     * @param max The maximum value.
     * @param length The length of the progress bar.
     * @param symbol The symbol used for the filled part of the bar.
     * @param filledColor The color for the filled part of the bar.
     * @param emptyColor The color for the empty part of the bar.
     * @return A string representation of the progress bar.
     */

    fun progressBar(value: Int, max: Int, length: Int = 10, symbol: String = "|", filledColor: String = "#e6ff59", emptyColor: String = "#AAAAAA"): String {
        return progressBar(value.toDouble(), max.toDouble(), length, symbol, filledColor, emptyColor)
    }
}
