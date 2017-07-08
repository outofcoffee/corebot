package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionRequest
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.driver.ActionDriverFactory
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * Initiates actions by triggering the appropriate driver directly.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class DirectActionPerformServiceImpl @Inject constructor(
        private val actionDriverFactory: ActionDriverFactory) : ActionPerformService {

    override fun perform(request: PerformActionRequest): CompletableFuture<PerformActionResult> {
        val actionDriver = actionDriverFactory.driverFor(request.actionConfig.driver)
        return actionDriver.perform(request.trigger, request.actionType, request.actionConfig, request.args)
    }
}
