package com.gatehill.corebot.chat.model.action

/**
 * Represents a system action to perform.
 *
 * All system actions have the 'all' tag.
 */
class SystemAction(override val actionType: ActionType,
                   override val shortDescription: String,
                   override val startMessage: String?) : Action(actionType, shortDescription, startMessage, listOf("all"))
