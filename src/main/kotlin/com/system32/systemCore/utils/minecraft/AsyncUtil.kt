package com.system32.systemCore.utils.minecraft

import com.system32.systemCore.utils.minecraft.ServerUtil.task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AsyncManager {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

class AsyncTask<T>(private val block: suspend () -> T) {
    private var onComplete: ((T) -> Unit)? = null

    infix fun onComplete(block: (T) -> Unit) {
        onComplete = block
    }

    fun launch() {
        AsyncManager.scope.launch {
            val result = block()
            task {
                onComplete?.invoke(result)
            }
        }
    }
}

fun <T> async(block: suspend () -> T): AsyncTask<T> {
    val t = AsyncTask(block)
    t.launch()
    return t
}