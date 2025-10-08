package com.system32dev.systemCore.utils.minecraft

import com.system32dev.systemCore.SystemCore
import org.bukkit.scheduler.BukkitRunnable

class CountdownTask(
    private val duration: Int,
    private val async: Boolean = false
) {
    private val secondCallbacks = mutableMapOf<Int, MutableList<(Int) -> Unit>>()
    private var onStart: (() -> Unit)? = null
    private var onTick: ((Int) -> Unit)? = null
    private var onFinish: (() -> Unit)? = null
    private var task: BukkitRunnable? = null
    private var _timeLeft = duration

    val timeLeft: Int
        get() = _timeLeft

    val isRunning: Boolean
        get() = task?.isCancelled == false

    fun onStart(block: () -> Unit) = apply { onStart = block }

    fun onTick(block: (Int) -> Unit) = apply { onTick = block }

    fun at(vararg seconds: Int, block: (Int) -> Unit) = apply {
        for (second in seconds) {
            secondCallbacks.getOrPut(second) { mutableListOf() }.add(block)
        }
    }

    fun atHalf(block: (Int) -> Unit) = apply {
        val half = duration / 2
        secondCallbacks.getOrPut(half) { mutableListOf() }.add(block)
    }

    fun onFinish(block: () -> Unit) = apply { onFinish = block }

    fun start(): CountdownTask {
        _timeLeft = duration

        onStart?.invoke()

        task = object : BukkitRunnable() {
            override fun run() {
                if (_timeLeft <= 0) {
                    onFinish?.invoke()
                    cancel()
                    return
                }

                onTick?.invoke(_timeLeft)
                secondCallbacks[_timeLeft]?.forEach { it(_timeLeft) }
                _timeLeft--
            }
        }

        if (async)
            task!!.runTaskTimerAsynchronously(SystemCore.plugin, 0L, 20L)
        else
            task!!.runTaskTimer(SystemCore.plugin, 0L, 20L)

        return this
    }

    fun cancel() {
        task?.cancel()
    }
}
