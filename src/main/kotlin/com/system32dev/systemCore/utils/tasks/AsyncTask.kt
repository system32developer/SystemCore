package com.system32dev.systemCore.utils.tasks

import org.bukkit.Bukkit
import com.system32dev.systemCore.SystemCore
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.CompletableFuture

fun <T> async(block: () -> T): CompletableFuture<T> {
    return CompletableFuture.supplyAsync(block)
}

fun <T> asyncTransaction(block: () -> T): CompletableFuture<T> {
    return CompletableFuture.supplyAsync {
        transaction {
            block()
        }
    }
}

fun <T> CompletableFuture<T>.onCompleteSync(action: (T) -> Unit): CompletableFuture<T> {
    return this.whenCompleteAsync({ result, ex ->
        if (ex == null) {
            action(result)
        }
    }, Bukkit.getScheduler().getMainThreadExecutor(SystemCore.plugin))
}

fun <T> CompletableFuture<T>.onError(action: (Throwable) -> Unit): CompletableFuture<T> {
    return this.exceptionally { ex ->
        action(ex)
        null
    }
}