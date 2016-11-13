package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class BaseActionDriver @Inject constructor(private val triggerJobService: TriggerJobService,
                                                    private val lockService: LockService) : ActionDriver {

    /**
     * Route the specified action to a handler.
     */
    override fun perform(channelId: String, triggerMessageSenderId: String, triggerMessageTimestamp: String,
                         actionType: ActionType, action: ActionConfig,
                         args: Map<String, String>): CompletableFuture<PerformActionResult> {

        val future = CompletableFuture<PerformActionResult>()
        try {
            when (actionType) {
                ActionType.TRIGGER -> triggerJobService.trigger(channelId, triggerMessageTimestamp, future, action, args)
                ActionType.LOCK -> lockService.acquireLock(future, action, triggerMessageSenderId)
                ActionType.UNLOCK -> lockService.unlock(future, action)

                else -> {
                    // delegate to driver
                    if (!handleAction(channelId, triggerMessageSenderId, triggerMessageTimestamp,
                            future, actionType, action, args)) throw UnsupportedOperationException(
                            "Action type ${actionType} is not supported by ${javaClass.canonicalName}")
                }
            }
        } catch(e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }

    /**
     * Attempt to handle the given action, with the specified arguments.
     */
    protected abstract fun handleAction(channelId: String, triggerMessageSenderId: String,
                                        triggerMessageTimestamp: String, future: CompletableFuture<PerformActionResult>,
                                        actionType: ActionType, action: ActionConfig,
                                        args: Map<String, String>): Boolean
}
