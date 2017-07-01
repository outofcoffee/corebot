package com.gatehill.corebot.driver.items.action.factory

import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import javax.inject.Inject

/**
 * Evict a borrower from an item.
 */
@Template("evictUserFromItem", placeholderKeys = arrayOf(
        EvictUserFromItemFactory.borrowerPlaceholder,
        BaseItemFactory.itemPlaceholder
))
class EvictUserFromItemFactory @Inject constructor(configService: ConfigService) : BaseItemFactory(configService) {
    override val actionType: ActionType = ItemsActionType.ITEM_EVICT_USER

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""

    companion object {
        const val borrowerPlaceholder = "borrower"
    }
}
