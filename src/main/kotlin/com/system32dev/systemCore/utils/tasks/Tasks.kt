package com.system32dev.systemCore.utils.tasks

import com.system32dev.systemCore.SystemCore
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

private fun convertToTicks(time: Int, unit: TimeUnit): Long {
    val ms = unit.toMillis(time.toLong())
    return ms / 50L
}

fun task(delay: Int = 0, unit: TimeUnit = TimeUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask {
    val ticks = convertToTicks(delay, unit)
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskLater(SystemCore.plugin, Runnable {
        block(task!!)
    }, ticks)
    return task
}

fun taskAsync(delay: Int = 0, unit: TimeUnit = TimeUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask {
    val ticks = convertToTicks(delay, unit)
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskLaterAsynchronously(SystemCore.plugin, Runnable {
        block(task!!)
    }, ticks)
    return task
}

fun taskLater(time: Int, unit: TimeUnit = TimeUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask = task(time, unit, block)

fun taskLaterAsync(time: Int, unit: TimeUnit = TimeUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask = taskAsync(time, unit, block)

fun taskTimer(
    period: Int,
    unit: TimeUnit = TimeUnit.SECONDS,
    block: (BukkitTask) -> Unit
): BukkitTask {
    val tickPeriod = convertToTicks(period, unit)
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskTimer(SystemCore.plugin, Runnable {
        block(task!!)
    }, 0L, tickPeriod)
    return task
}

fun taskTimerAsync(
    period: Int,
    unit: TimeUnit = TimeUnit.SECONDS,
    block: (BukkitTask) -> Unit
): BukkitTask {
    val tickPeriod = convertToTicks(period, unit)
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskTimerAsynchronously(SystemCore.plugin, Runnable {
        block(task!!)
    }, 0L, tickPeriod)
    return task
}