package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.ConfigService
import java.util.LinkedList
import javax.inject.Inject

/**
 * Locks an option value.
 */
class LockOptionTemplate @Inject constructor(configService: ConfigService,
                                             chatGenerator: ChatGenerator) : BaseLockOptionTemplate(configService, chatGenerator) {

    override val actionType: ActionType = CoreActionType.LOCK_OPTION
    override val tokens = LinkedList(listOf("lock", "{$optionNamePlaceholder}", "{$optionValuePlaceholder}"))
}
