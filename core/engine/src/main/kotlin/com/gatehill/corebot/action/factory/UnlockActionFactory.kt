package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.CoreActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Unlocks an action.
 */
@Template("unlockAction", builtIn = true, showInUsage = true, actionMessageMode = ActionMessageMode.INDIVIDUAL)
class UnlockActionFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val actionType: ActionType = CoreActionType.UNLOCK_ACTION
}
