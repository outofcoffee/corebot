package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.Action
import com.gatehill.corebot.chat.model.action.SystemAction
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Represents a system action.
 */
abstract class SystemActionTemplate : AbstractActionTemplate() {
    override val builtIn = true
    override val actionConfigs: List<ActionConfig> = emptyList()

    override fun buildActions(): List<Action> {
        return listOf(SystemAction(actionType, buildShortDescription(), buildMessage()))
    }
}
