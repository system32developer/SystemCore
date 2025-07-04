package com.system32.systemCore.utils.minecraft

import com.system32.systemCore.SystemCore
import com.system32.systemCore.utils.text.TextUtil
import com.system32.systemCore.utils.text.TextUtil.color
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

object ServerUtil {
    fun player(uuid: UUID): Player? {
        return Bukkit.getPlayer(uuid)
    }

    fun player(name: String) : Player?{
        return Bukkit.getPlayer(name)
    }

    fun offlinePlayer(name: String) : Player?{
        return Bukkit.getOfflinePlayer(name).player
    }

    fun offlinePlayer(uuid: UUID) : Player?{
        return Bukkit.getOfflinePlayer(uuid).player
    }

    fun players() : List<Player>{
        return Bukkit.getOnlinePlayers().toList()
    }

    fun broadcast(message: String) {
        Bukkit.broadcast(color(message))
    }

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