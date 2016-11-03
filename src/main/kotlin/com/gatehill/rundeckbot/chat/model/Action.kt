package com.gatehill.rundeckbot.chat.model

import com.gatehill.rundeckbot.chat.ActionType

/**
 * Represents an action to perform.
 */
abstract class Action(open val actionType: ActionType,
                      open val shortDescription: String,
                      open val actionMessage: String) {

    override fun toString(): String {
        return "Action(actionType=$actionType)"
    }
}
