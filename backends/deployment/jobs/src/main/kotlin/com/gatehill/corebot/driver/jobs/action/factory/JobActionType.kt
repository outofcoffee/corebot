package com.gatehill.corebot.driver.jobs.action.factory

import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.CoreActionType

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
