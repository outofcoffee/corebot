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
 * Borrow an item.
 */
@Template("borrowItem", builtIn = true, showInUsage = true, actionMessageMode = ActionMessageMode.INDIVIDUAL,
        placeholderKeys = arrayOf(
                BaseItemFactory.itemPlaceholder,
                BorrowItemFactory.subItemPlaceholder,
                BorrowItemFactory.reasonPlaceholder
        ))
open class BorrowItemFactory @Inject constructor(configService: ConfigService) : BaseItemFactory(configService) {
    override val actionType: ActionType = ItemsActionType.ITEM_BORROW

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""

    companion object {
        const val subItemPlaceholder = "optionalSubItemName"
        const val reasonPlaceholder = "reason"
    }
}
