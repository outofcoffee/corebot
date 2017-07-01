package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.CoreActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Unlocks an action.
 */
@Template("unlockAction")
class UnlockActionFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val actionType: ActionType = CoreActionType.UNLOCK_ACTION
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
}
