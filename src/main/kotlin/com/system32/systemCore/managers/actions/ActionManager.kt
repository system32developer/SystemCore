package com.system32.systemCore.managers.actions

import org.bukkit.entity.Player
import java.util.regex.Pattern

class ActionManager(private val actions: Iterable<String>) {

    private val allowedActions: MutableSet<String> = mutableSetOf()

    fun allow(vararg toAllow: String) {
        allowedActions.clear()
        allowedActions.addAll(toAllow.map { it.uppercase() })
    }

    fun disallow(vararg toDisallow: String) {
        toDisallow.forEach { allowedActions.remove(it.uppercase()) }
    }

    fun handle(player: Player) {
        executeNext(player, actions.toMutableList())
    }

    private fun executeNext(player: Player, remaining: MutableList<String>) {
        if (remaining.isEmpty()) return

        val raw = remaining.removeAt(0)
        val type = extractType(raw)?.uppercase() ?: run {
            executeNext(player, remaining)
            return
        }

        if (allowedActions.isNotEmpty() && !allowedActions.contains(type)) {
            executeNext(player, remaining)
            return
        }

        val data = applyPlaceholders(extractData(raw), player)
        val handler = ActionRegistry.get(type)

        if (handler != null) {
            val continueExecution = handler(player, data) {
                executeNext(player, remaining)
            }
            if (!continueExecution) return
        }

        executeNext(player, remaining)
    }

    private fun extractType(input: String): String? {
        val matcher = Pattern.compile("^\\[(\\w+)]\\s+.*", Pattern.CASE_INSENSITIVE).matcher(input)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractData(input: String): String {
        val matcher = Pattern.compile("^\\[\\w+]\\s+(.*)", Pattern.CASE_INSENSITIVE).matcher(input)
        return if (matcher.find()) matcher.group(1).trim() else ""
    }

    private fun applyPlaceholders(message: String, player: Player): String {
        val placeholders = mapOf(
            "%player%" to player.name,
            "%world%" to player.world.name,
            "%x%" to player.location.blockX.toString(),
            "%y%" to player.location.blockY.toString(),
            "%z%" to player.location.blockZ.toString()
        )

        var result = message
        for ((key, value) in placeholders) {
            result = result.replace(key, value)
        }
        return result
    }
}