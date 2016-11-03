package com.gatehill.rundeckbot.action

import com.gatehill.rundeckbot.action.model.PerformActionResult
import com.gatehill.rundeckbot.config.model.ActionConfig
import com.ullink.slack.simpleslackapi.SlackUser
import java.util.concurrent.CompletableFuture

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object LockService {
    /**
     * A lock held on a job.
     */
    data class Lock(val owner: SlackUser)

    private val locks: MutableMap<ActionConfig, Lock> = mutableMapOf()

    fun acquireLock(future: CompletableFuture<PerformActionResult>, action: ActionConfig, sender: SlackUser) {
        val lock = locks[action]
        if (null != lock) {
            if (lock.owner == sender) {
                // already locked by self
                future.complete(PerformActionResult("BTW, you already had the lock for *${action.name}* :wink:"))

            } else {
                // locked by someone else
                future.completeExceptionally(IllegalStateException(
                        "The lock for ${action.name} is already held by <@${lock.owner.id}>"))
            }

        } else {
            // acquire
            locks[action] = Lock(sender)
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
