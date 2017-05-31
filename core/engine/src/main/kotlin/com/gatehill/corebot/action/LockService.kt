package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.chat.model.template.BaseLockOptionTemplate
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.partition
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Named

/**
 * Allows an action to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class LockService @Inject constructor(@Named("lockStore") private val lockStore: DataStore) {
    /**
     * A lock held on an action.
     */
    data class ActionLock(val owner: String)

    /**
     * A lock held on an Option.
     */
    data class OptionLock(val owner: String,
                          val optionName: String,
                          val optionValue: String)

    private val actionLocks
        get() = lockStore.partition<String, ActionLock>("actionLocks")

    private val optionLocks
        get() = lockStore.partition<String, OptionLock>("optionLocks")

    fun lockAction(future: CompletableFuture<PerformActionResult>, action: ActionConfig,
                   triggerMessageSenderId: String) {

        checkActionLock(action) { lock ->
            lock?.let {
                if (lock.owner == triggerMessageSenderId) {
                    // already locked by self
                    future.complete(PerformActionResult("BTW, you already had the lock for *${action.name}* :wink:"))

                } else {
                    // locked by someone else
                    future.completeExceptionally(IllegalStateException(
                            "The lock for ${action.name} is already held by <@${lock.owner}>"))
                }

            } ?: run {
                // acquire
                actionLocks[action.name] = ActionLock(triggerMessageSenderId)
                future.complete(PerformActionResult("OK, I've locked :lock: *${action.name}* for you."))
            }
        }
    }

    fun unlockAction(future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        checkActionLock(action) { lock ->
            lock?.let {
                // unlock
                actionLocks.remove(action.name)
                future.complete(PerformActionResult("OK, I've unlocked :unlock: *${action.name}* for you."))

            } ?: run {
                // already unlocked
                future.complete(PerformActionResult("BTW, *${action.name}* was already unlocked :wink:"))
            }
        }
    }

    fun checkActionLock(action: ActionConfig, callback: (ActionLock?) -> Unit) = callback(actionLocks[action.name])

    fun lockOption(future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>,
                   triggerMessageSenderId: String) {

        val optionName = args[BaseLockOptionTemplate.optionNamePlaceholder]!!
        val optionValue = args[BaseLockOptionTemplate.optionValuePlaceholder]!!

        val lock = optionLocks[action.name]
        if (isOptionLocked(lock, optionName, optionValue)) {
            if (lock!!.owner == triggerMessageSenderId) {
                // already locked by self
                future.complete(PerformActionResult())

            } else {
                // locked by someone else
                future.completeExceptionally(IllegalStateException(
                        "The lock for $optionName $optionValue is already held by <@${lock.owner}>"))
            }

        } else {
            // acquire
            optionLocks[action.name] = OptionLock(
                    triggerMessageSenderId,
                    optionName,
                    optionValue)

            future.complete(PerformActionResult())
        }
    }

    fun unlockOption(future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>) {
        val optionName = args[BaseLockOptionTemplate.optionNamePlaceholder]!!
        val optionValue = args[BaseLockOptionTemplate.optionValuePlaceholder]!!

        val lock = optionLocks[action.name]
        if (isOptionLocked(lock, optionName, optionValue)) {
            optionLocks.remove(action.name)
        }

        future.complete(PerformActionResult())
    }

    private fun isOptionLocked(lock: OptionLock?, optionName: String, optionValue: String) =
            lock?.let { it.optionName == optionName && it.optionValue == optionValue } ?: false

    fun checkOptionLock(action: ActionConfig, args: Map<String, String>, callback: (OptionLock?) -> Unit) {
        val lock = optionLocks[action.name]

        val locked = lock?.let {
            args.filter { arg -> arg.key.equals(lock.optionName, ignoreCase = true) }
                    .filter { arg -> arg.value == lock.optionValue }
                    .any()
        } ?: false

        callback(if (locked) lock else null)
    }
}
