package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.Action
import com.gatehill.corebot.action.model.SystemAction
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Represents a system action.
 */
abstract class SystemActionFactory : BaseActionFactory() {
    override val builtIn = true
    override val actionConfigs = emptyList<ActionConfig>()

    override fun buildActions(trigger: TriggerContext): List<Action> {
        return listOf(SystemAction(actionType,
                buildShortDescription(),
                if (actionMessageMode == ActionMessageMode.INDIVIDUAL) buildStartMessage(trigger) else null))
    }
}
