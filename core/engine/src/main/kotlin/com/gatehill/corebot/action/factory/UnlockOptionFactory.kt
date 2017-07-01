package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.CoreActionType
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Unlocks an option value.
 */
@Template("unlockOption")
class UnlockOptionFactory @Inject constructor(configService: ConfigService,
                                              chatGenerator: ChatGenerator) : BaseLockOptionFactory(configService, chatGenerator) {

    override val actionType: ActionType = CoreActionType.UNLOCK_OPTION
}
