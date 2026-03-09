package com.system32dev.systemCore.utils.tasks

import com.system32dev.systemCore.SystemCore
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

val Int.ticks: Long
    get() = this.toLong()

val Int.seconds: Long
    get() = this * 20L

val Int.minutes: Long
    get() = this * 20L * 60

val Int.hours: Long
    get() = this * 20L * 60 * 60

val Int.days: Long
    get() = this * 20L * 60 * 60 * 24


fun task(delay: Int = 0, unit: TaskUnit = TaskUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask {
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskLater(SystemCore.plugin, Runnable {
        block(task!!)
    }, unit.toTicks(delay))
    return task
}

fun taskAsync(delay: Int = 0, unit: TaskUnit = TaskUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask {
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskLaterAsynchronously(SystemCore.plugin, Runnable {
        block(task!!)
    }, unit.toTicks(delay))
    return task
}

fun taskLater(time: Int, unit: TaskUnit = TaskUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask =
    task(time, unit, block)

fun taskLaterAsync(time: Int, unit: TaskUnit = TaskUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask =
    taskAsync(time, unit, block)

fun taskTimer(period: Int, unit: TaskUnit = TaskUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask {
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskTimer(SystemCore.plugin, Runnable {
        block(task!!)
    }, 0L, unit.toTicks(period))
    return task
}

fun taskTimerAsync(period: Int, unit: TaskUnit = TaskUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask {
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskTimerAsynchronously(SystemCore.plugin, Runnable {
        block(task!!)
    }, 0L, unit.toTicks(period))
    return task
}

fun task(delayTicks: Long, block: (BukkitTask) -> Unit): BukkitTask {
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskLater(SystemCore.plugin, Runnable { block(task!!) }, delayTicks)
    return task
}

fun taskAsync(delayTicks: Long, block: (BukkitTask) -> Unit): BukkitTask {
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskLaterAsynchronously(SystemCore.plugin, Runnable { block(task!!) }, delayTicks)
    return task
}

fun taskTimer(periodTicks: Long, block: (BukkitTask) -> Unit): BukkitTask {
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskTimer(SystemCore.plugin, Runnable { block(task!!) }, 0L, periodTicks)
    return task
}

fun taskTimerAsync(periodTicks: Long, block: (BukkitTask) -> Unit): BukkitTask {
    var task: BukkitTask? = null
    task = Bukkit.getScheduler().runTaskTimerAsynchronously(SystemCore.plugin, Runnable { block(task!!) }, 0L, periodTicks)
    return task
}