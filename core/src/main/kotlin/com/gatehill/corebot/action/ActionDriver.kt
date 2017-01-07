package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig
import java.util.concurrent.CompletableFuture

/**
 * Represents a driver for performing an action.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ActionDriver {
    fun perform(trigger: TriggerContext, actionType: ActionType, action: ActionConfig,
                args: Map<String, String>): CompletableFuture<PerformActionResult>
}
