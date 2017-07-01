package com.gatehill.corebot.driver.items.action.factory

import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import javax.inject.Inject

/**
 * Evict all borrowers from an item.
 */
@Template("evictItem")
class EvictItemFactory @Inject constructor(configService: ConfigService) : BaseItemFactory(configService) {
    override val actionType: ActionType = ItemsActionType.ITEM_EVICT
    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""
}
