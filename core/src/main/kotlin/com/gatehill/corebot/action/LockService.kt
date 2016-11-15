package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.chat.BaseLockOptionTemplate
import com.gatehill.corebot.chat.LockOptionTemplate
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
    data class ActionLock(val owner: String)

    /**
     * A lock held on an Option.
     */
    data class OptionLock(val owner: String,
                          val optionName: String,
                          val optionValue: String)

    private val actionLocks = mutableMapOf<ActionConfig, ActionLock>()
    private val optionLocks = mutableMapOf<ActionConfig, OptionLock>()

    fun lockAction(future: CompletableFuture<PerformActionResult>, action: ActionConfig,
                   triggerMessageSenderId: String) {

        val lock = actionLocks[action]
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
            actionLocks[action] = ActionLock(triggerMessageSenderId)
            future.complete(PerformActionResult("OK, I've locked :lock: *${action.name}* for you."))
        }
    }

    fun unlockAction(future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        checkActionLock(action) { lock ->
            if (null != lock) {
                // unlock
                actionLocks.remove(action)
                future.complete(PerformActionResult("OK, I've unlocked :unlock: *${action.name}* for you."))

            } else {
                // already unlocked
                future.complete(PerformActionResult("BTW, *${action.name}* was already unlocked :wink:"))
            }
        }
    }

    fun checkActionLock(action: ActionConfig, callback: (ActionLock?) -> Unit) = callback(actionLocks[action])

    fun lockOption(future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>,
                   triggerMessageSenderId: String) {

        val optionName = args[BaseLockOptionTemplate.optionNamePlaceholder]!!
        val optionValue = args[BaseLockOptionTemplate.optionValuePlaceholder]!!

        val lock = optionLocks[action]
        if (null != lock) {
            if (lock.owner == triggerMessageSenderId) {
                // already locked by self
                future.complete(PerformActionResult("BTW, you already had the lock for *${optionName} ${optionValue}* :wink:"))

            } else {
                // locked by someone else
                future.completeExceptionally(IllegalStateException(
                        "The lock for ${optionName} ${optionValue} is already held by <@${lock.owner}>"))
            }

        } else {
            // acquire
            optionLocks[action] = OptionLock(
                    triggerMessageSenderId,
                    optionName,
                    optionValue)

            future.complete(PerformActionResult("OK, I've locked :lock: *${optionName} ${optionValue}* for you."))
        }
    }

    fun unlockOption(future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>) {
        val optionName = args[BaseLockOptionTemplate.optionNamePlaceholder]!!
        val optionValue = args[BaseLockOptionTemplate.optionValuePlaceholder]!!

        checkOptionLock(action, args) { lock ->
            if (null != lock) {
                // unlock
                optionLocks.remove(action)
                future.complete(PerformActionResult("OK, I've unlocked :unlock: *${optionName} ${optionValue}* for you."))

            } else {
                // already unlocked
                future.complete(PerformActionResult("BTW, *${optionName} ${optionValue}* was already unlocked :wink:"))
            }
        }
    }

    fun checkOptionLock(action: ActionConfig, args: Map<String, String>, callback: (OptionLock?) -> Unit) {
        val lock = optionLocks[action]

        val locked = lock?.let {
            args.filter { arg -> arg.key.equals(lock.optionName, ignoreCase = true) }
                    .filter { arg -> arg.value == lock.optionValue }
                    .any()
        } ?: false

        callback(if (locked) lock else null)
    }
}
