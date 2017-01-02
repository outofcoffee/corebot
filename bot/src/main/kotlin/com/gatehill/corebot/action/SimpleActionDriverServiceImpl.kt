package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionRequest
import com.gatehill.corebot.action.model.PerformActionResult
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * Initiates actions locally by triggering the appropriate driver directly.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class SimpleActionDriverServiceImpl @Inject constructor(
        private val actionDriverFactory: ActionDriverFactory) : ActionDriverService {

    override fun perform(request: PerformActionRequest): CompletableFuture<PerformActionResult> {
        val actionDriver = actionDriverFactory.driverFor(request.actionConfig.driver)

        return actionDriver.perform(request.channelId,
                request.triggerMessageSenderId,
                request.triggerMessageTimestamp,
                request.actionType,
                request.actionConfig,
                request.args)
    }
}
