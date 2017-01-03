package com.gatehill.corebot.action.model

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig

/**
 * A request to perform an action.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class PerformActionRequest(val channelId: String,
                                val triggerMessageSenderId: String,
                                val triggerMessageTimestamp: String,
                                val actionType: ActionType,
                                val actionConfig: ActionConfig,
                                val args: Map<String, String>) {

    companion object Builder {
        fun build(channelId: String, triggerMessageSenderId: String, triggerMessageTimestamp: String,
                  actionType: ActionType, actionConfig: ActionConfig, args: Map<String, String>): PerformActionRequest {

            return PerformActionRequest(channelId, triggerMessageSenderId, triggerMessageTimestamp,
                    actionType, actionConfig, args)
        }
    }
}
