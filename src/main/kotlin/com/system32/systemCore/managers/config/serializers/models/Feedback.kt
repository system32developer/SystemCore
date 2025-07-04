package com.system32.systemCore.managers.config.serializers.models

import com.system32.systemCore.utils.text.TextUtil.color
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Represents a preformatted message that can be sent to players, the console, or any [CommandSender].
 *
 * The message is parsed using MiniMessage (and optionally PlaceholderAPI if supported).
 * It is cached as an Adventure [Component] for performance.
 *
 * @property message The raw message string, written in MiniMessage format.
 */
class Feedback(val message: String) {

    /**
     * The colored and parsed [Component] version of the message, lazily initialized.
     */
    private val coloredMessage: Component by lazy { color(message) }

    /**
     * Sends the message to the console.
     */
    fun send() {
        Bukkit.getConsoleSender().sendMessage(coloredMessage)
    }

    /**
     * Sends the message to the player only if the given condition is true.
     *
     * @param condition Boolean condition to check.
     * @param audience The target audience (can be a player, console, commandsender, entity, server, world, team)
     */
    fun sendIf(condition: Boolean, audience: Audience) {
        if (condition) audience.sendMessage(coloredMessage)
    }

    /**
     * Sends the message to multiple players.
     *
     * @param audience Vararg of [Audience] to send the message to.
     */
    fun send(vararg audience: Audience) {
        audience.forEach { it.sendMessage(coloredMessage) }
    }

    /**
     * Sends the message to a collection of players.
     *
     * @param audience Iterable of [Audience] to send the message to.
     */
    fun send(audience: Iterable<Audience>) {
        audience.forEach { it.sendMessage(coloredMessage) }
    }

    /**
     * Sends the message to players near a given player within a certain radius.
     *
     * @param center The central player used for distance calculation.
     * @param radius The maximum distance to receive the message.
     */
    fun send(center: Player, radius: Double) {
        center.location.getNearbyPlayers(radius).forEach { it.sendMessage(coloredMessage) }
    }

    /**
     * Sends the message to all online players.
     */
    fun broadcast() {
        Bukkit.getOnlinePlayers().forEach { it.sendMessage(coloredMessage) }
    }

    /**
     * Sends the message to all online players if the given condition is true.
     *
     * @param condition Boolean condition to check.
     */
    fun broadcastIf(condition: Boolean) {
        if (condition) Bukkit.getOnlinePlayers().forEach { it.sendMessage(coloredMessage) }
    }

    /**
     * Sends the message to all players near a given location within a radius.
     *
     * @param center The central location.
     * @param radius The maximum distance to receive the message.
     */
    fun broadcast(center: Location, radius: Double) {
        center.getNearbyPlayers(radius).forEach { it.sendMessage(coloredMessage) }
    }

    /**
     * Returns the raw MiniMessage-formatted string.
     */
    override fun toString(): String = message
}