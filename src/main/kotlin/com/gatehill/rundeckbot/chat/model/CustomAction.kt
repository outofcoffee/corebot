package com.gatehill.rundeckbot.chat.model

import com.gatehill.rundeckbot.chat.ActionType
import com.gatehill.rundeckbot.config.model.ActionConfig

/**
 * Represents a custom action to perform.
 */
class CustomAction(override val actionType: ActionType,
                   override val shortDescription: String,
                   override val actionMessage: String,
                   override val tags: List<String>,
                   val actionConfig: ActionConfig,
                   val args: Map<String, String>) : Action(actionType, shortDescription, actionMessage, tags) {

    override fun toString(): String {
        return "CustomAction(actionType=$actionType, actionConfig=$actionConfig, args=$args)"
    }
}
