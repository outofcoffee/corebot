package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.ConfigService
import java.util.*
import javax.inject.Inject

/**
 * Prints status information about a job.
 */
class StatusJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = CoreActionType.STATUS
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
    override val tokens = LinkedList(listOf("status", "{$actionPlaceholder}"))
}