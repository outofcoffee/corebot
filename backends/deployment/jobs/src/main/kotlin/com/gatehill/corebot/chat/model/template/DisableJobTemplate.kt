package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.ConfigService
import java.util.*
import javax.inject.Inject

/**
 * Disables a job.
 */
class DisableJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = JobActionType.DISABLE
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
    override val tokens = LinkedList(listOf("disable", "{${actionPlaceholder}}"))
}