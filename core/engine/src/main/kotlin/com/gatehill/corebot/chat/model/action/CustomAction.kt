package com.gatehill.corebot.chat.model.action

import com.gatehill.corebot.config.model.ActionConfig

/**
 * Represents a custom action to perform.
 */
class CustomAction(override val actionType: ActionType,
                   override val shortDescription: String,
                   override val startMessage: String?,
                   override val tags: List<String>,
                   val driver: String,
                   val actionConfig: ActionConfig,
                   val args: Map<String, String>) : Action(actionType, shortDescription, startMessage, tags) {

    override fun toString(): String {
        return "CustomAction(actionType=$actionType, actionConfig=$actionConfig, args=$args)"
    }
}
