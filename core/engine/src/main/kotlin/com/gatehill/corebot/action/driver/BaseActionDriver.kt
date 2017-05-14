package com.gatehill.corebot.action.driver

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.model.ActionConfig
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class BaseActionDriver @Inject constructor(private val lockService: LockService) : ActionDriver {
    /**
     * Route the specified action to a handler.
     */
    override fun perform(trigger: TriggerContext, actionType: ActionType, action: ActionConfig,
                         args: Map<String, String>): CompletableFuture<PerformActionResult> {

        val future = CompletableFuture<PerformActionResult>()
        try {
            when (actionType) {
                CoreActionType.LOCK_ACTION -> lockService.lockAction(future, action, trigger.userId)
                CoreActionType.UNLOCK_ACTION -> lockService.unlockAction(future, action)
                CoreActionType.STATUS -> showStatus(future, action)
                CoreActionType.LOCK_OPTION -> lockService.lockOption(future, action, args, trigger.userId)
                CoreActionType.UNLOCK_OPTION -> lockService.unlockOption(future, action, args)
                else -> {
                    // delegate to driver
                    if (!handleAction(trigger, future, actionType, action, args)) throw UnsupportedOperationException(
                            "Action type $actionType is not supported by ${javaClass.canonicalName}")
                }
            }
        } catch(e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }

    private fun showStatus(future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        val msg = StringBuilder("Status of *${action.name}*: ")

        lockService.checkActionLock(action) { lock ->
            lock?.let {
                msg.append("locked :lock: by <@${lock.owner}>")
            } ?: run {
                msg.append("unlocked :unlock:")
            }

            future.complete(PerformActionResult(msg.toString()))
        }
    }

    /**
     * Attempt to handle the given action, with the specified arguments.
     */
    protected abstract fun handleAction(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                                        actionType: ActionType, action: ActionConfig,
                                        args: Map<String, String>): Boolean
}
