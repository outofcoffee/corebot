package com.gatehill.corebot.action.model

/**
 * Represents a system action to perform.
 *
 * All system actions have the 'all' tag.
 */
class SystemAction(override val actionType: ActionType,
                   override val shortDescription: String,
                   override val startMessage: String?) : Action(actionType, shortDescription, startMessage, listOf("all"))
