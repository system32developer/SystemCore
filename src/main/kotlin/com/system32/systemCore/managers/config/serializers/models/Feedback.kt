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

    val inlineText : String = message.joinToString(separator = "\n")

    /**
     * The colored and parsed [Component] version of the message, lazily initialized.
     */
    private val coloredMessage: List<Component> by lazy { color(message) }

    /**
     * Sends the message to the console.
     */
    fun send() {
        coloredMessage.forEach { Bukkit.getConsoleSender().sendMessage(it) }
    }

    /**
     * Sends the message to the player only if the given condition is true.
     *
     * @param condition Boolean condition to check.
     * @param audience The target audience (can be a player, console, commandsender, entity, server, world, team)
     */
    fun sendIf(condition: Boolean, audience: Audience) {
        if (condition) coloredMessage.forEach { audience.sendMessage(it) }
    }

    /**
     * Sends the message to multiple players.
     *
     * @param audience Vararg of [Audience] to send the message to.
     */
    fun send(vararg audience: Audience) {
        audience.forEach { target -> coloredMessage.forEach { target.sendMessage(it) } }
    }

    /**
     * Sends the message to a collection of players.
     *
     * @param audience Iterable of [Audience] to send the message to.
     */
    fun send(audience: Iterable<Audience>) {
        audience.forEach { target -> coloredMessage.forEach { target.sendMessage(it) } }
    }

    /**
     * Sends the message to players near a given player within a certain radius.
     *
     * @param center The central player used for distance calculation.
     * @param radius The maximum distance to receive the message.
     */
    fun send(center: Player, radius: Double) {
        center.location.getNearbyPlayers(radius).forEach { target -> coloredMessage.forEach { target.sendMessage(it) } }
    }

    /**
     * Sends the message to all online players.
     */
    fun broadcast() {
        Bukkit.getOnlinePlayers().forEach { target -> coloredMessage.forEach { target.sendMessage(it) } }
    }

    /**
     * Sends the message to all online players if the given condition is true.
     *
     * @param condition Boolean condition to check.
     */
    fun broadcastIf(condition: Boolean) {
        if (condition) Bukkit.getOnlinePlayers().forEach { target -> coloredMessage.forEach { target.sendMessage(it) } }
    }

    /**
     * Sends the message to all players near a given location within a radius.
     *
     * @param center The central location.
     * @param radius The maximum distance to receive the message.
     */
    fun broadcast(center: Location, radius: Double) {
        center.getNearbyPlayers(radius).forEach { target -> coloredMessage.forEach { target.sendMessage(it) } }
    }

    /**
     * Sends the message to a specific audience with placeholders replaced.
     *
     * @param audience The target audience (can be a player, console, commandsender, entity, server, world, team)
     * @param placeholders A map of placeholders to replace in the message.
     */

    fun send(audience: Audience, placeholders: Map<String, String>) {
        coloredMessage.forEach { audience.sendMessage(inlineText.withPlaceholders(placeholders)) }
    }

    /**
     * Sends the message to multiple audiences with placeholders replaced.
     *
     * @param audience Vararg of [Audience] to send the message to.
     * @param placeholders A map of placeholders to replace in the message.
     */

    fun send(audience: Iterable<Audience>, placeholders: Map<String, String>) {
        audience.forEach { target -> coloredMessage.forEach { target.sendMessage(inlineText.withPlaceholders(placeholders)) } }
    }

    /**
     * Sends the message to players near a given player within a certain radius, with placeholders replaced.
     *
     * @param center The central player used for distance calculation.
     * @param radius The maximum distance to receive the message.
     * @param placeholders A map of placeholders to replace in the message.
     */

    fun send(center: Player, radius: Double, placeholders: Map<String, String>) {
        center.location.getNearbyPlayers(radius).forEach { target ->
            coloredMessage.forEach { target.sendMessage(inlineText.withPlaceholders(placeholders)) }
        }
    }

    /**
     * Sends the message to all online players with placeholders replaced.
     *
     * @param placeholders A map of placeholders to replace in the message.
     */

    fun broadcast(placeholders: Map<String, String>) {
        Bukkit.getOnlinePlayers().forEach { target ->
            coloredMessage.forEach { target.sendMessage(inlineText.withPlaceholders(placeholders)) }
        }
    }

    /**
     * Sends the message to all online players if the given condition is true, with placeholders replaced.
     *
     * @param condition Boolean condition to check.
     * @param placeholders A map of placeholders to replace in the message.
     */

    fun broadcast(center: Location, radius: Double, placeholders: Map<String, String>) {
        center.getNearbyPlayers(radius).forEach { target ->
            coloredMessage.forEach { target.sendMessage(inlineText.withPlaceholders(placeholders)) }
        }
    }

    /**
     * Sends the message to a specific audience with placeholders replaced.
     *
     * @param placeholders A map of placeholders to replace in the message.
     */

    private fun String.withPlaceholders(placeholders: Map<String, String>): Component {

        return MiniMessage.miniMessage().deserialize(this, *placeholders.map { (key, value) -> Placeholder.parsed(key, value) }.toTypedArray())
    }

}