package com.system32dev.systemCore.utils.tasks

enum class TaskUnit {
    TICKS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS;

    fun toTicks(amount: Int): Long = when(this) {
        TICKS -> amount.toLong()
        SECONDS -> amount * 20L
        MINUTES -> amount * 20L * 60
        HOURS   -> amount * 20L * 60 * 60
        DAYS    -> amount * 20L * 60 * 60 * 24
    }
}