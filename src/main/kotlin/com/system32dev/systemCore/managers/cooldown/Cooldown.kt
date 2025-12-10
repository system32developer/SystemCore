package com.system32dev.systemCore.managers.cooldown

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

            val parts = listOf(
                formatUnit(days, "dias"),
                formatUnit(hours, "horas"),
                formatUnit(minutes, "minutos"),
                formatUnit(seconds, "segundos")
            ).filterNot { it.startsWith("0") }

            return if (parts.isEmpty()) "1 segundo" else parts.joinToString(" con ")
        }
}