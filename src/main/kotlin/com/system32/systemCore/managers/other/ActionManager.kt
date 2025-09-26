package com.system32.systemCore.managers.other

import com.system32.systemCore.utils.minecraft.ServerUtil.taskLater
import com.system32.systemCore.utils.text.TextUtil.color
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import java.time.Duration
import java.util.regex.Pattern

class ActionManager (private val actions: Iterable<String>) {

    private val allowedActions: MutableList<ActionType> = ActionType.entries.toMutableList()

    fun allow(vararg toAllow: ActionType) {
        allowedActions.clear()
        allowedActions.addAll(toAllow)
    }

    fun disallow(vararg toDisallow: ActionType) {
        toDisallow.forEach { allowedActions.remove(it) }
    }

    fun handle(player: Player) {
        executeNext(player, actions.toMutableList())
    }

    private fun executeNext(player: Player, remaining: MutableList<String>) {
        if (remaining.isEmpty()) return

        val raw = remaining.removeAt(0)
        val type = extractType(raw)?.let {
            runCatching { ActionType.valueOf(it.uppercase()) }.getOrNull()
        } ?: run {
            executeNext(player, remaining)
            return
        }

        if (!allowedActions.contains(type)) {
            executeNext(player, remaining)
            return
        }

        val data = applyPlaceholders(extractData(raw), player)

        when (type) {
            ActionType.SUDO -> player.performCommand(data)
            ActionType.DISPATCH -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), data)
            ActionType.BROADCAST -> Bukkit.broadcast(color(data))
            ActionType.MESSAGE -> player.sendMessage(color(data))
            ActionType.PLAY_SOUND -> {
                val parts = data.split(" ")
                val sound = Sound.valueOf(parts[0].uppercase())
                val pitch = parts.getOrNull(1)?.toFloatOrNull() ?: 1.0f
                player.playSound(player.location, sound, SoundCategory.MASTER, 1.0f, pitch)
            }
            ActionType.DELAY -> {
                val delayTicks = (data.toIntOrNull() ?: 1) * 20L
                taskLater(delayTicks) {
                    executeNext(player, remaining)
                }
                return
            }
            ActionType.TITLE -> {
                val parts = data.split(";", limit = 5)
                val title = if (parts.isNotEmpty()) color(parts[0]) else Component.empty()
                val subtitle = if (parts.size > 1) color(parts[1]) else Component.empty()
                val fadeIn = parts.getOrNull(2)?.toLongOrNull() ?: 10
                val stay = parts.getOrNull(3)?.toLongOrNull() ?: 70
                val fadeOut = parts.getOrNull(4)?.toLongOrNull() ?: 20
                player.showTitle(Title.title(title, subtitle, Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut))))
            }
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

    enum class ActionType {
        SUDO,
        DISPATCH,
        BROADCAST,
        MESSAGE,
        PLAY_SOUND,
        DELAY,
        TITLE
    }
}