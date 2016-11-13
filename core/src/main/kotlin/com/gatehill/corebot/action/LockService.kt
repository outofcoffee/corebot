package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.config.model.ActionConfig
import java.util.concurrent.CompletableFuture

/**
 * Allows an action to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class LockService {
    /**
     * A lock held on an action.
     */
    data class Lock(val owner: String)

    private val locks: MutableMap<ActionConfig, Lock> = mutableMapOf()

    fun acquireLock(future: CompletableFuture<PerformActionResult>, action: ActionConfig,
                    triggerMessageSenderId: String) {

        val lock = locks[action]
        if (null != lock) {
            if (lock.owner == triggerMessageSenderId) {
                // already locked by self
                future.complete(PerformActionResult("BTW, you already had the lock for *${action.name}* :wink:"))

            } else {
                // locked by someone else
                future.completeExceptionally(IllegalStateException(
                        "The lock for ${action.name} is already held by <@${lock.owner}>"))
            }

        } else {
            // acquire
            locks[action] = Lock(triggerMessageSenderId)
            future.complete(PerformActionResult("OK, I've locked :lock: *${action.name}* for you."))
        }
    }

    fun unlock(future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        val lock = locks[action]
        if (null != lock) {
            // unlock
            locks.remove(action)
            future.complete(PerformActionResult("OK, I've unlocked :unlock: *${action.name}* for you."))

        } else {
            // already unlocked
            future.complete(PerformActionResult("BTW, *${action.name}* was already unlocked :wink:"))
        }
    }

    fun checkLock(action: ActionConfig, callback: (Lock?) -> Unit) {
        callback(locks[action])
    }
}
