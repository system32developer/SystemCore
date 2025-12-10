package com.system32dev.systemCore.managers.actions

import com.system32dev.systemCore.utils.color
import com.system32dev.systemCore.utils.tasks.taskLater
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

object ActionRegistry {

    private val actions = ConcurrentHashMap<String, ActionHandler>()

    /**
     * Registers a new action handler with the specified ID.
     *
     * @param id The unique identifier for the action (case-insensitive).
     * @param handler The [ActionHandler] implementation to handle the action.
     *
     * ### Example usage:
     * ```
     * ActionRegistry.register("CUSTOM_ACTION") { player, data, next ->
     *     // Custom action logic here
     *     player.sendMessage("Custom action executed with data: $data")
     *     next() // Call next to continue to the next action (optional)
     *     true // Return true to indicate the action was handled
     * }
     * ```
     */
    fun register(id: String, handler: ActionHandler) {
        actions[id.uppercase()] = handler
    }

    fun get(id: String): ActionHandler? = actions[id.uppercase()]

    init {
        register("SUDO") { player, data, _ ->
            player.performCommand(data)
            true
        }

        register("DISPATCH") { _, data, _ ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), data)
            true
        }

        register("BROADCAST") { _, data, _ ->
            Bukkit.broadcast(color(data))
            true
        }

        register("MESSAGE") { player, data, _ ->
            player.sendMessage(color(data))
            true
        }

        register("CLOSE") { player, data, _ ->
            player.closeInventory()
            true
        }

        register("PLAY_SOUND") { player, data, _ ->
            val parts = data.split(" ")
            val sound = Sound.valueOf(parts[0].uppercase())
            val pitch = parts.getOrNull(1)?.toFloatOrNull() ?: 1.0f
            player.playSound(player.location, sound, SoundCategory.MASTER, 1.0f, pitch)
            true
        }

        register("DELAY") { _, data, next ->
            val delayTicks = (data.toIntOrNull() ?: 1) * 20L
            taskLater(delayTicks) { next() }
            false
        }

        register("TITLE") { player, data, _ ->
            val parts = data.split(";", limit = 5)
            val title = if (parts.isNotEmpty()) color(parts[0]) else Component.empty()
            val subtitle = if (parts.size > 1) color(parts[1]) else Component.empty()
            val fadeIn = parts.getOrNull(2)?.toLongOrNull() ?: 10
            val stay = parts.getOrNull(3)?.toLongOrNull() ?: 70
            val fadeOut = parts.getOrNull(4)?.toLongOrNull() ?: 20
            player.showTitle(
                Title.title(
                    title, subtitle,
                    Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut))
                )
            )
            true
        }
    }
}