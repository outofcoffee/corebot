package com.gatehill.corebot.action.model

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig

/**
 * A request to perform an action.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class PerformActionRequest(val trigger: TriggerContext,
                                val actionType: ActionType,
                                val actionConfig: ActionConfig,
                                val args: Map<String, String>) {

    companion object Builder {
        fun build(trigger: TriggerContext, actionType: ActionType, actionConfig: ActionConfig,
                  args: Map<String, String>): PerformActionRequest {

            return PerformActionRequest(trigger, actionType, actionConfig, args)
        }
    }
}
