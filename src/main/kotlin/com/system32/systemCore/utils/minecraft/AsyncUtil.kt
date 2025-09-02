package com.system32.systemCore.utils.minecraft

import com.system32.systemCore.utils.minecraft.ServerUtil.task
import java.util.concurrent.CompletableFuture

class AsyncTask<T>(private val block: () -> T) {
    private val future = CompletableFuture<T>()

    fun launch() {
        CompletableFuture.supplyAsync {
            block()
        }.thenAccept { result ->
            future.complete(result)
        }.exceptionally { ex ->
            future.completeExceptionally(ex)
            null
        }
    }

    fun onComplete(action: (T) -> Unit): AsyncTask<T> {
        future.thenAccept { result ->
            task{ action(result) }
        }
        return this
    }
}

fun <T> async(block: () -> T): AsyncTask<T> {
    val task = AsyncTask(block)
    task.launch()
    return task
}