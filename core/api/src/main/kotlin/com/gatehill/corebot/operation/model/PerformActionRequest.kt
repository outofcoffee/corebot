package com.gatehill.corebot.operation.model

import com.gatehill.corebot.config.model.ActionConfig

/**
 * A request to perform an action.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class PerformActionRequest(val trigger: TriggerContext,
                                val operationType: OperationType,
                                val actionConfig: ActionConfig,
                                val args: Map<String, String>) {

    companion object Builder {
        fun build(trigger: TriggerContext, operationType: OperationType, actionConfig: ActionConfig,
                  args: Map<String, String>): PerformActionRequest {

            return PerformActionRequest(trigger, operationType, actionConfig, args)
        }
    }
}
