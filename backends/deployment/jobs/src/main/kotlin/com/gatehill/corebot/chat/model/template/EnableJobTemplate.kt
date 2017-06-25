package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Enables a job.
 */
class EnableJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = JobActionType.ENABLE
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
}