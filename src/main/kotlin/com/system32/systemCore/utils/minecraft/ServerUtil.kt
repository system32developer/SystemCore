package com.system32.systemCore.utils.minecraft

import com.system32.systemCore.SystemCore
import com.system32.systemCore.utils.text.TextUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class ServerUtil {
    companion object{
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
            Bukkit.broadcast(TextUtil.Companion.color(message))
        }

        fun task(runnable: Runnable) : BukkitTask {
            return Bukkit.getScheduler().runTask(SystemCore.plugin, runnable)
        }

        fun taskLater(runnable: Runnable, delay: Int) : BukkitTask {
            return Bukkit.getScheduler().runTaskLater(SystemCore.plugin, runnable, 20L * delay)
        }

        fun taskTimer(runnable: Runnable, delay: Int, period: Int) : BukkitTask {
            return Bukkit.getScheduler().runTaskTimer(SystemCore.plugin, runnable, 20L * delay, 20L * period)
        }

        fun taskAsync(runnable: Runnable) : BukkitTask {
            return Bukkit.getScheduler().runTaskAsynchronously(SystemCore.plugin, runnable)
        }

        fun taskLaterAsync(runnable: Runnable, delay: Int) : BukkitTask {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(SystemCore.plugin, runnable, 20L * delay)
        }

        fun taskTimerAsync(runnable: Runnable, delay: Int, period: Int) : BukkitTask {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(SystemCore.plugin, runnable, 20L * delay, 20L * period)
        }

        //With Long

        fun taskLater(runnable: Runnable, delay: Long) : BukkitTask {
            return Bukkit.getScheduler().runTaskLater(SystemCore.plugin, runnable, delay)
        }

        fun taskTimer(runnable: Runnable, delay: Long, period: Long) : BukkitTask {
            return Bukkit.getScheduler().runTaskTimer(SystemCore.plugin, runnable, delay, period)
        }

        fun taskLaterAsync(runnable: Runnable, delay: Long) : BukkitTask {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(SystemCore.plugin, runnable, delay)
        }

        fun taskTimerAsync(runnable: Runnable, delay: Long, period: Long) : BukkitTask {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(SystemCore.plugin, runnable, delay, period)
        }
    }
}