package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Prints status information about an action.
 */
class StatusActionTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = CoreActionType.STATUS
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
}
