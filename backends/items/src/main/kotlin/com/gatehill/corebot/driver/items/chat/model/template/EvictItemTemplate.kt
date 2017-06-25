package com.gatehill.corebot.driver.items.chat.model.template

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import javax.inject.Inject

/**
 * Evict all borrowers from an item.
 */
class EvictItemTemplate @Inject constructor(configService: ConfigService) : BaseItemTemplate(configService) {
    override val actionType: ActionType = ItemsActionType.ITEM_EVICT
    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""
}
