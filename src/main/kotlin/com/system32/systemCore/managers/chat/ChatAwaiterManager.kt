package com.system32.systemCore.managers.chat

import com.system32.systemCore.utils.text.TextUtil.Companion.asText
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.function.Consumer


class ChatAwaiterManager : Listener {
    private val awaiters: MutableMap<String, Consumer<ChatInput>> = HashMap<String, Consumer<ChatInput>>()

    /**
     * Adds a chat awaiter for a specific player.
     *
     * @param player The player's name.
     * @param callback The function to execute when the player sends a message.
     */
    fun addAwaiter(player: String, callback: Consumer<ChatInput>) {
        awaiters.put(player, callback)
    }

    /**
     * Checks if a player has an active chat awaiter.
     *
     * @param player The player's name.
     * @return True if the player has an active awaiter, false otherwise.
     */
    fun hasAwaiter(player: String): Boolean {
        return awaiters.containsKey(player)
    }

    /**
     * Retrieves and removes the awaiter for a specific player.
     *
     * @param player The player's name.
     * @return The Consumer function if present, null otherwise.
     */
    fun getAwaiter(player: String): Consumer<ChatInput> {
        return awaiters[player] ?: throw IllegalStateException("No awaiter found for player: $player")
    }

    /**
     * Removes an active chat awaiter for a specific player.
     *
     * @param player The player's name.
     */
    fun removeAwaiter(player: String) {
        awaiters.remove(player)
    }

    /**
     * Clears all chat awaiters.
     */
    fun clearAllAwaiters() {
        awaiters.clear()
    }

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.getPlayer()
        if (awaiters.containsKey(player.name)) {
            val callback = awaiters.remove(player.name)
            if (callback != null) {
                callback.accept(ChatInput(player, asText(event.message())))
                event.isCancelled = true
            }
        }
    }
}