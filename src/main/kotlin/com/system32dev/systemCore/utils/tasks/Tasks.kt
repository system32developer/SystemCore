package com.system32dev.systemCore.utils.tasks

import com.system32dev.systemCore.SystemCore
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

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