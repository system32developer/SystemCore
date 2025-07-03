package com.system32.systemCore.managers.chat

import com.system32.systemCore.SystemCore
import com.system32.systemCore.utils.text.TextUtil.asText
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player

object ChatTriggerManager : Listener {

    init {
        SystemCore.event(this)
    }

    private val triggers = mutableMapOf<String, (ChatInput) -> Unit>()

    fun addTrigger(vararg keywords: String, action: (ChatInput) -> Unit): ChatTriggerManager {
        keywords.forEach { keyword ->
            triggers[keyword.lowercase()] = action
        }
        return this
    }

    fun addTrigger(keywords: List<String>, action: (ChatInput) -> Unit): ChatTriggerManager {
        keywords.forEach { keyword ->
            triggers[keyword.lowercase()] = action
        }
        return this
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val message = asText(event.message()).trim().lowercase()
        val words = message.split(" ").filter { it.isNotEmpty() }

        if (words.isEmpty()) return

        val keyword = words[0]
        val args = words.drop(1).toTypedArray()

        triggers[keyword]?.let { action ->
            action(ChatInput(player, args))
            event.isCancelled = true
        }
    }
}
