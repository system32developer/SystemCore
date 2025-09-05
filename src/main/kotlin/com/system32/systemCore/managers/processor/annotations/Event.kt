package com.system32.systemCore.managers.processor.annotations

import org.bukkit.event.EventPriority

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class Event(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false
)
