package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.CoreActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Prints status information about an action.
 */
@Template("statusAction", builtIn = true, showInUsage = true, actionMessageMode = ActionMessageMode.INDIVIDUAL)
class StatusActionFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val actionType: ActionType = CoreActionType.STATUS
}
