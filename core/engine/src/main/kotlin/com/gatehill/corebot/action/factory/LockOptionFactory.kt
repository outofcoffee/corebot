package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.CoreActionType
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Locks an option value.
 */
@Template("lockOption")
class LockOptionFactory @Inject constructor(configService: ConfigService,
                                            chatGenerator: ChatGenerator) : BaseLockOptionFactory(configService, chatGenerator) {

    override val actionType: ActionType = CoreActionType.LOCK_OPTION
}
