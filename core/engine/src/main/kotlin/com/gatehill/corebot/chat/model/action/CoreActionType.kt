package com.gatehill.corebot.chat.model.action

/**
 * The core action types.
 */
open class CoreActionType(name: String, description: String) : ActionType(name, description) {
    companion object {
        val HELP = ActionType("HELP", "show help")
        val LOCK_ACTION = ActionType("LOCK_ACTION", "lock action")
        val UNLOCK_ACTION = ActionType("UNLOCK_ACTION", "unlock action")
        val STATUS = ActionType("STATUS", "check status")
        val LOCK_OPTION = ActionType("LOCK_OPTION", "lock option")
        val UNLOCK_OPTION = ActionType("UNLOCK_OPTION", "unlock option")
    }
}
