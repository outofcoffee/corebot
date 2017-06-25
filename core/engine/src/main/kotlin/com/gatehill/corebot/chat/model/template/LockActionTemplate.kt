package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Locks an action.
 */
class LockActionTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = CoreActionType.LOCK_ACTION
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
}