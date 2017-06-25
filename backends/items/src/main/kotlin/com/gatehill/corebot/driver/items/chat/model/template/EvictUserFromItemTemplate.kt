package com.gatehill.corebot.driver.items.chat.model.template

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.template.RegexActionTemplate
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import javax.inject.Inject

/**
 * Evict a borrower from an item.
 */
class EvictUserFromItemTemplate @Inject constructor(configService: ConfigService) : BaseItemTemplate(configService), RegexActionTemplate {
    override val actionType: ActionType = ItemsActionType.ITEM_EVICT_USER
    override val placeholderKeys = listOf(borrowerPlaceholder, itemPlaceholder)

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""

    companion object {
        val borrowerPlaceholder = "borrower"
    }
}
