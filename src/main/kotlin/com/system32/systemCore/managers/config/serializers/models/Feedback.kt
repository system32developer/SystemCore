package com.system32.systemCore.managers.config.serializers.models

import com.system32.systemCore.utils.text.TextUtil.asText
import com.system32.systemCore.utils.text.TextUtil.color
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.util.regex.Pattern

/**
 * Represents a preformatted message that can be sent to players, the console, or any [CommandSender].
 *
 * The message is parsed using MiniMessage (and optionally PlaceholderAPI if supported).
 * It is cached as an Adventure [Component] for performance.
 *
 * @property message The raw message string, written in MiniMessage format.
 */
class Feedback(val message: List<String>) {

    /**
     * Creates a [Feedback] instance from a single MiniMessage-formatted string.
     *
     * @param line The single message line.
     */
    constructor(line: String) : this(listOf(line))

    /**
     * Creates a [Feedback] instance from multiple lines using vararg.
     *
     * @param lines The lines of the message.
     */
    constructor(vararg lines: String) : this(lines.toList())

    /**
     * Whether the message contains multiple lines.
     */
    val isList: Boolean
        get() = message.size > 1

    val inlineText : String = message.joinToString("\n")

    fun log(tag: TagResolver? = null) =  Bukkit.getConsoleSender().sendMessage(color(inlineText, tag))

    fun send(tag: TagResolver? = null, vararg audience: Audience) = audience.forEach { target -> target.sendMessage(color(inlineText, tag))}

    fun send(audience: Iterable<Audience>, tag: TagResolver? = null) = audience.forEach { target -> target.sendMessage(color(inlineText, tag))}

    fun send(audience: Audience, tag: TagResolver? = null) = audience.sendMessage(color(inlineText, tag))

    fun send(player: Player, tag: TagResolver? = null, radius: Int = 0) {
        if(radius > 0) {
            send(Audience.audience(player.location.getNearbyPlayers(radius.toDouble()).toList()), tag)
            return
        }
        player.sendMessage(color(inlineText, tag))
    }

    fun broadcast(tag: TagResolver? = null) = Bukkit.getOnlinePlayers().forEach { target -> target.sendMessage(color(inlineText, tag)) }

    class Serializer  : TypeSerializer<Feedback> {

        override fun deserialize(type: Type?, node: ConfigurationNode?): Feedback? {
            if (node == null) return null
            return Feedback(if(node.isList) node.getList(String::class.java)!! else listOf(node.string?: "No message provided"))
        }

        override fun serialize(type: Type?, obj: Feedback?, node: ConfigurationNode?) {
            if (node == null) return
            if (obj == null) {
                node.set(null)
                println("No Component to save, setting node to null")
                return
            }

            if (!obj.isList) node.set(obj.message[0]) else node.setList(String::class.java, obj.message)
        }
    }

}