package com.system32.systemCore.gui.components

import org.bukkit.event.Event


fun interface GuiAction<T : Event> {
    fun execute(event: T)
}
