package com.system32dev.systemCore.managers.chat

import org.bukkit.entity.Player

class ChatInput {
    val player: Player
    val response: String?
    val args: Array<String>?

    constructor(player: Player, response: String) {
        this.player = player
        this.response = response
        this.args = null
    }

    constructor(player: Player, args: Array<String>) {
        this.player = player
        this.response = null
        this.args = args
    }
}