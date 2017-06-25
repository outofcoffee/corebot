package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Unlocks an action.
 */
class UnlockActionTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = CoreActionType.UNLOCK_ACTION
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
}
