package com.system32.systemCore.utils.minecraft

import com.system32.systemCore.SystemCore
import org.bukkit.scheduler.BukkitRunnable

class CountdownTask(
    private val duration: Int,
    private val async: Boolean = false
) {
    private val secondCallbacks = mutableMapOf<Int, MutableList<() -> Unit>>()
    private var onTick: ((Int) -> Unit)? = null
    private var onFinish: (() -> Unit)? = null
    private var task: BukkitRunnable? = null
    private var _timeLeft = duration

    val timeLeft: Int
        get() = _timeLeft

    val isRunning: Boolean
        get() = task?.isCancelled == false

    fun onTick(block: (Int) -> Unit) = apply { onTick = block }

    fun at(second: Int, block: () -> Unit) = apply {
        secondCallbacks.getOrPut(second) { mutableListOf() }.add(block)
    }

    fun onFinish(block: () -> Unit) = apply { onFinish = block }

    fun start(): CountdownTask {
        _timeLeft = duration

        task = object : BukkitRunnable() {
            override fun run() {
                if (_timeLeft <= 0) {
                    onFinish?.invoke()
                    cancel()
                    return
                }

                onTick?.invoke(_timeLeft)
                secondCallbacks[_timeLeft]?.forEach { it() }
                _timeLeft--
            }
        }

        if (async) task!!.runTaskTimerAsynchronously(SystemCore.plugin, 0L, 20L) else task!!.runTaskTimer(SystemCore.plugin, 0L, 20L)

        return this
    }

    fun cancel() {
        task?.cancel()
    }
}
