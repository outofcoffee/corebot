package com.gatehill.corebot.chat.model.action

/**
 * Represents an action to perform.
 */
abstract class Action(open val actionType: ActionType,
                      open val shortDescription: String,
                      open val actionMessage: String,
                      open val tags: List<String>) {

    override fun toString(): String {
        return "Action(actionType=$actionType)"
    }
}
