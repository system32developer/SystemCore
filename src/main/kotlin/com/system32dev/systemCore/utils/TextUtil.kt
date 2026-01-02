package com.system32dev.systemCore.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.text.DecimalFormat

object TextUtil {

    const val CENTER_PX: Int = 154
    const val NORMAL_LINE: String = "&7&m-----------------------------"

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

private val mini = MiniMessage.miniMessage()

fun tag(vararg tags: Pair<String, Any>): TagResolver {
    return TagResolver.resolver(
        tags.map { (placeholder, value) ->
            TagResolver.resolver(placeholder, Tag.selfClosingInserting(color(value.toString())))
        }
    )
}

fun color(input: String, tag: TagResolver? = null): Component {
    if (input.isEmpty()) return Component.empty()
    var message = input.replace("§", "&")
    if(message.contains("&")) message = mini.serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(message)).replace("\\", "")
    if(message.startsWith("<center>")) message = centerMessage(message.replace("<center>", ""))
    val component = if(tag != null) mini.deserialize(message, tag) else mini.deserialize(message)
    return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
}

fun color(messages: List<String>, tag: TagResolver? = null): List<Component> {
    return messages.map { color(it, tag) }
}

fun formatNumber(value: Any): String {
    val number = when (value) {
        is Int -> value.toLong()
        is String -> value.toLongOrNull() ?: return value
        else -> throw IllegalArgumentException("The value must be an Int or String.")
    }

    val formatter = DecimalFormat("#,###")
    return formatter.format(number)
}

fun Component.asText(): String = mini.serialize(this)

fun Component.asPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)

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
    val toCompensate = TextUtil.CENTER_PX - halvedMessageSize
    val spaceLength = FontInfo.SPACE.length + 1
    var compensated = 0
    val sb = StringBuilder()

    while (compensated < toCompensate) {
        sb.append(" ")
        compensated += spaceLength
    }

    return sb.toString() + message
}

fun String.caseFirst(): String {
    return this.substring(0, 1).uppercase() + this.substring(1).lowercase()
}

fun normalize(value: Number, max: Number): Double {
    return (value.toDouble() / max.toDouble()).coerceIn(0.0, 1.0)
}