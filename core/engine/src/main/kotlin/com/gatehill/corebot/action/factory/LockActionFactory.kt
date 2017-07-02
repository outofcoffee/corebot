package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.CoreActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Locks an action.
 */
@Template("lockAction", builtIn = true, showInUsage = true, actionMessageMode = ActionMessageMode.INDIVIDUAL)
class LockActionFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val actionType: ActionType = CoreActionType.LOCK_ACTION
}
