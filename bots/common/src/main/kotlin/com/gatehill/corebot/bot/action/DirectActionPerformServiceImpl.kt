package com.gatehill.corebot.bot.action

import com.gatehill.corebot.action.ActionPerformService
import com.gatehill.corebot.operation.model.PerformActionRequest
import com.gatehill.corebot.operation.model.PerformActionResult
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
        return actionDriver.perform(request.trigger, request.operationType, request.actionConfig, request.args)
    }
}
