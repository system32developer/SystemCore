package com.system32dev.systemCore.managers.actions

import org.bukkit.entity.Player

fun interface ActionHandler {
    operator fun invoke(player: Player, data: String, next: () -> Unit): Boolean
}