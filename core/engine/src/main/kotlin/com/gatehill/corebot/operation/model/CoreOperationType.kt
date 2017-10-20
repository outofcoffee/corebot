package com.gatehill.corebot.operation.model

/**
 * The core operation types.
 */
open class CoreOperationType(name: String, description: String) : OperationType(name, description) {
    companion object {
        val HELP = OperationType("HELP", "show help")
        val LOCK_ACTION = OperationType("LOCK_ACTION", "lock action")
        val UNLOCK_ACTION = OperationType("UNLOCK_ACTION", "unlock action")
        val STATUS_ACTION = OperationType("STATUS_ACTION", "check action status")
        val LOCK_OPTION = OperationType("LOCK_OPTION", "lock option")
        val UNLOCK_OPTION = OperationType("UNLOCK_OPTION", "unlock option")
        val STATUS_OPTION = OperationType("STATUS_OPTION", "check option status")
    }
}
