package com.gatehill.corebot.util

import java.util.concurrent.CompletableFuture

/**
 * Syntactic sugar for handling `exceptionally()` flow.
 */
fun CompletableFuture<*>.onException(handler: (Throwable) -> Unit) {
    this.exceptionally {
        handler(it)
        null
    }
}
