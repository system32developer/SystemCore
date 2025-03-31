package com.system32.systemCore.managers.chat

import com.system32.systemCore.SystemCore
import com.system32.systemCore.utils.text.TextUtil.Companion.asText
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player

class ChatTriggerManager : Listener {


    private val triggers = mutableMapOf<String, (ChatInput) -> Unit>()

    fun addTrigger(keyword: String, action: (ChatInput) -> Unit): ChatTriggerManager {
        triggers[keyword.lowercase()] = action
        return this
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val message = asText(event.message()).lowercase()

        triggers.keys.firstOrNull { message.startsWith(it) }?.let { keyword ->
            val args = message.substringAfter(keyword).trim().split(" ").filter { it.isNotEmpty() }.toTypedArray()
            triggers[keyword]?.invoke(ChatInput(player, args))
            event.isCancelled = true
        }
    }
}
