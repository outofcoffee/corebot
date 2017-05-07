package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.ConfigService
import java.util.*
import javax.inject.Inject

/**
 * Enables a job.
 */
class EnableJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.ENABLE
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
    override val tokens = LinkedList(listOf("enable", "{${actionPlaceholder}}"))
}