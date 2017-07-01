package com.gatehill.corebot.action.model

/**
 * Groups actions and tracks successful executions.
 */
data class ActionWrapper(val actions: List<Action>,
                         val groupStartMessage: String?,
                         val groupCompleteMessage: String?) {

    var successful: Int = 0
}
