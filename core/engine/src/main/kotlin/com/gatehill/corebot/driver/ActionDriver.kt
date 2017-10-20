package com.gatehill.corebot.driver

import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import java.util.concurrent.CompletableFuture

/**
 * Represents a driver for performing an action.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ActionDriver {
    fun perform(trigger: TriggerContext, operationType: OperationType, action: ActionConfig,
                args: Map<String, String>): CompletableFuture<PerformActionResult>
}
