package com.system32dev.systemCore.utils.minecraft

import com.system32dev.systemCore.SystemCore
import com.system32dev.systemCore.utils.text.TextUtil
import com.system32dev.systemCore.utils.text.TextUtil.color
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.TimeUnit

object ServerUtil {

    fun addItems(
        inventory: Inventory,
        items: Iterable<ItemStack>,
        onFail: (List<ItemStack>) -> Unit = {}
    ): List<ItemStack> {
        val leftover = inventory.addItem(*items.toList().toTypedArray()).values.toList()
        if (leftover.isNotEmpty()) onFail(leftover)

        return leftover
    }

    fun addItems(
        inventory: Inventory,
        vararg items: ItemStack,
        onFail: (List<ItemStack>) -> Unit = {}
    ): List<ItemStack> = addItems(inventory, items.toList(), onFail)

    private fun convertToTicks(time: Long, unit: TimeUnit): Long {
        val ms = unit.toMillis(time)
        return ms / 50L
    }

    fun task(delay: Long = 0, unit: TimeUnit = TimeUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask {
        val ticks = convertToTicks(delay, unit)
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskLater(SystemCore.plugin, Runnable {
            block(task!!)
        }, ticks)
        return task
    }

    fun taskAsync(delay: Long = 0, unit: TimeUnit = TimeUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask {
        val ticks = convertToTicks(delay, unit)
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskLaterAsynchronously(SystemCore.plugin, Runnable {
            block(task!!)
        }, ticks)
        return task
    }

    fun taskLater(time: Long, unit: TimeUnit = TimeUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask = task(time, unit, block)

    fun taskLaterAsync(time: Long, unit: TimeUnit = TimeUnit.SECONDS, block: (BukkitTask) -> Unit): BukkitTask = taskAsync(time, unit, block)

    fun taskTimer(
        period: Long,
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
        period: Long,
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

    @Deprecated("Use task(delay, unit, block) with TimeUnit", ReplaceWith("task(0, TimeUnit.SECONDS, block)"))
    fun task(block: (BukkitTask) -> Unit): BukkitTask =
        task(0, TimeUnit.SECONDS, block)


    @Deprecated("Use taskLater(delay, unit, block)", ReplaceWith("taskLater(delay, TimeUnit.SECONDS, block)"))
    fun taskLater(delay: Int, block: (BukkitTask) -> Unit): BukkitTask =
        taskLater(delay.toLong(), TimeUnit.SECONDS, block)


    @Deprecated("Use taskTimer(period, unit, block)", ReplaceWith("taskTimer(period, TimeUnit.SECONDS, block)"))
    fun taskTimer(period: Int, block: (BukkitTask) -> Unit): BukkitTask =
        taskTimer(period.toLong(), TimeUnit.SECONDS, block)


    @Deprecated("Use taskAsync(delay, unit, block)", ReplaceWith("taskAsync(0, TimeUnit.SECONDS, block)"))
    fun taskAsync(block: (BukkitTask) -> Unit): BukkitTask =
        taskAsync(0, TimeUnit.SECONDS, block)


    @Deprecated("Use taskLaterAsync(delay, unit, block)", ReplaceWith("taskLaterAsync(delay, TimeUnit.SECONDS, block)"))
    fun taskLaterAsync(delay: Int, block: (BukkitTask) -> Unit): BukkitTask =
        taskLaterAsync(delay.toLong(), TimeUnit.SECONDS, block)


    @Deprecated("Use taskTimerAsync(period, unit, block)", ReplaceWith("taskTimerAsync(period, TimeUnit.SECONDS, block)"))
    fun taskTimerAsync(period: Int, block: (BukkitTask) -> Unit): BukkitTask =
        taskTimerAsync(period.toLong(), TimeUnit.SECONDS, block)
}