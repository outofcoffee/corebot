package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.ConfigService
import java.util.*
import javax.inject.Inject

/**
 * Unlocks an option value.
 */
class UnlockOptionTemplate @Inject constructor(configService: ConfigService) : BaseLockOptionTemplate(configService) {
    override val actionType: ActionType = CoreActionType.UNLOCK_OPTION
    override val tokens = LinkedList(listOf("unlock", "{${optionNamePlaceholder}}", "{${optionValuePlaceholder}}"))
}