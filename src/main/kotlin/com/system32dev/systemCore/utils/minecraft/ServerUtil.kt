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

    fun task(block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTask(SystemCore.plugin, Runnable {
            block(task!!)
        })
        return task
    }

    fun taskLater(delay: Int, block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskLater(SystemCore.plugin, Runnable {
            block(task!!)
        }, 20L * delay)
        return task
    }

    fun taskTimer(delay: Int, period: Int, block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskTimer(SystemCore.plugin, Runnable {
            block(task!!)
        }, 20L * delay, 20L * period)
        return task
    }

    fun taskAsync(block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskAsynchronously(SystemCore.plugin, Runnable {
            block(task!!)
        })
        return task
    }

    fun taskLaterAsync(delay: Int, block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskLaterAsynchronously(SystemCore.plugin, Runnable {
            block(task!!)
        }, 20L * delay)
        return task
    }

    fun taskTimerAsync(delay: Int, period: Int, block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(SystemCore.plugin, Runnable {
            block(task!!)
        }, 20L * delay, 20L * period)
        return task
    }

// --- With Long versions ---

    fun taskLater(delay: Long, block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskLater(SystemCore.plugin, Runnable {
            block(task!!)
        }, delay)
        return task
    }

    fun taskTimer(delay: Long, period: Long, block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskTimer(SystemCore.plugin, Runnable {
            block(task!!)
        }, delay, period)
        return task
    }

    fun taskLaterAsync(delay: Long, block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskLaterAsynchronously(SystemCore.plugin, Runnable {
            block(task!!)
        }, delay)
        return task
    }

    fun taskTimerAsync(delay: Long, period: Long, block: (BukkitTask) -> Unit): BukkitTask {
        var task: BukkitTask? = null
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(SystemCore.plugin, Runnable {
            block(task!!)
        }, delay, period)
        return task
    }
}