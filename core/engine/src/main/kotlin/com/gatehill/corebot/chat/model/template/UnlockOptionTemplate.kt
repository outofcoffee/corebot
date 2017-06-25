package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Unlocks an option value.
 */
class UnlockOptionTemplate @Inject constructor(configService: ConfigService,
                                               chatGenerator: ChatGenerator) : BaseLockOptionTemplate(configService, chatGenerator) {

    override val actionType: ActionType = CoreActionType.UNLOCK_OPTION
}
