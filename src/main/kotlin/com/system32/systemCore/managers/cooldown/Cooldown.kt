package com.system32.systemCore.managers.cooldown

import com.system32.systemCore.managers.language.Language

data class Cooldown(val expirationTime: Long) {

    fun getTimeLeft(): Long {
        return (expirationTime - System.currentTimeMillis()).coerceAtLeast(0) / 1000
    }

    val time: String
        get() {
            val remaining = getTimeLeft()
            val days = remaining / 86400
            val hours = (remaining % 86400) / 3600
            val minutes = (remaining % 3600) / 60
            val seconds = remaining % 60

            return if (days > 0) {
                "%02d:%02d:%02d:%02d".format(days, hours, minutes, seconds)
            } else {
                "%02d:%02d:%02d".format(hours, minutes, seconds)
            }
        }

    val verbose: String
        get() {
            val remaining = getTimeLeft()
            val days = remaining / 86400
            val hours = (remaining % 86400) / 3600
            val minutes = (remaining % 3600) / 60
            val seconds = remaining % 60

            fun formatUnit(value: Long, unit: String): String {
                return if (value == 1L) "$value ${unit.dropLast(1)}" else "$value $unit"
            }

            return listOf(
                formatUnit(days, Language.TIME_UNIT_DAY()),
                formatUnit(hours, Language.TIME_UNIT_HOUR()),
                formatUnit(minutes, Language.TIME_UNIT_MINUTE()),
                formatUnit(seconds, Language.TIME_UNIT_SECOND())
            ).filter { !it.startsWith("0") }.joinToString(" with ")
        }
}
