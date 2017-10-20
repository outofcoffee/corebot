package com.gatehill.corebot.driver

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.CoreOperationType
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
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
    override fun perform(trigger: TriggerContext, operationType: OperationType, action: ActionConfig,
                         args: Map<String, String>): CompletableFuture<PerformActionResult> {

        val future = CompletableFuture<PerformActionResult>()
        try {
            when (operationType) {
                CoreOperationType.LOCK_ACTION -> lockService.lockAction(future, action, trigger.userId)
                CoreOperationType.UNLOCK_ACTION -> lockService.unlockAction(future, action)
                CoreOperationType.STATUS_ACTION -> showActionStatus(future, action)
                else -> {
                    // delegate to driver
                    if (!handleAction(trigger, future, operationType, action, args)) throw UnsupportedOperationException(
                            "Operation type $operationType is not supported by ${javaClass.canonicalName}")
                }
            }
        } catch (e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }

    private fun showActionStatus(future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        lockService.checkActionLock(action) { lock ->
            future.complete(PerformActionResult(lockService.describeLockStatus("Status of *${action.name}*: ", lock)))
        }
    }

    /**
     * Attempt to handle the given action, with the specified arguments.
     */
    protected abstract fun handleAction(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                                        operationType: OperationType, action: ActionConfig,
                                        args: Map<String, String>): Boolean
}
