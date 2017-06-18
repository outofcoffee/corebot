package com.gatehill.corebot.action

import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.partition
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Named

/**
 * Allows resources to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class LockService @Inject constructor(@Named("lockStore") private val lockStore: DataStore,
                                      private val chatGenerator: ChatGenerator) {

    private val logger = LogManager.getLogger(LockService::class.java)!!

    /**
     * A lock held on a resource.
     */
    interface BaseLock {
        val owner: String
    }

    /**
     * A lock held on an action.
     */
    data class ActionLock(override val owner: String) : BaseLock

    /**
     * A lock held on an option.
     */
    data class OptionLock(override val owner: String,
                          val optionName: String,
                          val optionValue: String) : BaseLock

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
                future.complete(PerformActionResult("${chatGenerator.confirmation()}, I've locked :lock: *${action.name}* for you."))
            }
        }
    }

    fun unlockAction(future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        checkActionLock(action) { lock ->
            lock?.let {
                // unlock
                actionLocks.remove(action.name)
                future.complete(PerformActionResult("${chatGenerator.confirmation()}, I've unlocked :unlock: *${action.name}* for you."))

            } ?: run {
                // already unlocked
                future.complete(PerformActionResult("BTW, *${action.name}* was already unlocked :wink:"))
            }
        }
    }

    fun checkActionLock(action: ActionConfig, callback: (ActionLock?) -> Unit) = callback(actionLocks[action.name])

    fun lockOption(optionName: String, optionValue: String, triggerMessageSenderId: String) {
        val lockName = buildOptionLockName(optionName, optionValue)

        val lock = optionLocks[lockName]
        if (isOptionLocked(lock, optionName, optionValue)) {
            if (lock!!.owner == triggerMessageSenderId) {
                // already locked by self
                logger.debug("Lock for $optionName $optionValue already held by $triggerMessageSenderId")

            } else {
                // locked by someone else
                throw IllegalStateException(
                        "The lock for $optionName $optionValue is already held by <@${lock.owner}>")
            }

        } else {
            // acquire
            optionLocks[lockName] = OptionLock(
                    triggerMessageSenderId,
                    optionName,
                    optionValue)

            logger.debug("Lock for $optionName $optionValue acquired by $triggerMessageSenderId")
        }
    }

    fun unlockOption(optionName: String, optionValue: String) {
        val lockName = buildOptionLockName(optionName, optionValue)

        val lock = optionLocks[lockName]
        if (isOptionLocked(lock, optionName, optionValue)) {
            optionLocks.remove(lockName)
        }

        logger.debug("Lock for $optionName $optionValue released")
    }

    private fun buildOptionLockName(optionName: String, optionValue: String) = "$optionName-$optionValue"

    private fun isOptionLocked(lock: OptionLock?, optionName: String, optionValue: String) =
            lock?.let { it.optionName == optionName && it.optionValue == optionValue } ?: false

    fun checkOptionLock(optionName: String, optionValue: String, callback: ((OptionLock?) -> Unit)? = null): OptionLock? {
        val lockName = buildOptionLockName(optionName, optionValue)
        val lock = optionLocks[lockName]

        val locked = lock?.let {
            optionName.equals(lock.optionName, ignoreCase = true) && optionValue == lock.optionValue
        } ?: false

        callback?.let { callback(if (locked) lock else null) }
        return lock
    }

    fun describeLockStatus(prefix: String, lock: BaseLock?): String = StringBuilder(prefix).apply {
        lock?.let {
            append("locked :lock: by <@${lock.owner}>")
        } ?: run {
            append("unlocked :unlock:")
        }
    }.toString()
}
