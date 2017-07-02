package com.gatehill.corebot.driver.items.action.factory

import com.gatehill.corebot.action.factory.ActionMessageMode
import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import javax.inject.Inject

/**
 * Return a borrowed item.
 */
@Template("returnItem", builtIn = true, showInUsage = true, actionMessageMode = ActionMessageMode.INDIVIDUAL)
class ReturnItemFactory @Inject constructor(configService: ConfigService) : BaseItemFactory(configService) {
    override val actionType: ActionType = ItemsActionType.ITEM_RETURN
    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""
}
