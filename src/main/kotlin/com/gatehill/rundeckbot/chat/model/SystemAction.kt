package com.gatehill.rundeckbot.chat.model

import com.gatehill.rundeckbot.chat.ActionType

/**
 * Represents a system action to perform.
 */
class SystemAction(override val actionType: ActionType,
                   override val shortDescription: String,
                   override val actionMessage: String) : Action(actionType, shortDescription, actionMessage)
