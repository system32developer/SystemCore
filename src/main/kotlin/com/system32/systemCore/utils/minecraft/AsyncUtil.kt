package com.system32.systemCore.utils.minecraft

import org.bukkit.Bukkit
import com.system32.systemCore.SystemCore
import java.util.concurrent.CompletableFuture

class AsyncTask<T>(private val block: () -> T) {
    private val future = CompletableFuture<T>()

    fun launch() {
        CompletableFuture.supplyAsync {
            block()
        }.whenComplete { result, ex ->
            if (ex != null) future.completeExceptionally(ex) else future.complete(result)
        }
    }

    fun onComplete(action: (T) -> Unit): AsyncTask<T> {
        future.thenAcceptAsync(
            { result -> action(result) },
            Bukkit.getScheduler().getMainThreadExecutor(SystemCore.plugin)
        )
        return this
    }
}

fun <T> async(block: () -> T): AsyncTask<T> {
    val task = AsyncTask(block)
    task.launch()
    return task
}
