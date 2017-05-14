package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType

/**
 * Extends the action types with job specific actions.
 */
class JobActionType(name: String, description: String) : CoreActionType(name, description) {
    companion object {
        val TRIGGER = ActionType("TRIGGER", "trigger job")
        val ENABLE = ActionType("ENABLE", "enable job")
        val DISABLE = ActionType("DISABLE", "disable job")
    }
}
