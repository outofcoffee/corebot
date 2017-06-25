package com.gatehill.corebot.driver.items.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.template.RegexActionTemplate
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import javax.inject.Inject

/**
 * Borrow an item as another user.
 */
class BorrowItemAsUserTemplate @Inject constructor(configService: ConfigService) : BorrowItemTemplate(configService), RegexActionTemplate {
    override val actionType: ActionType = ItemsActionType.ITEM_BORROW_AS_USER
    override val placeholderKeys = listOf(borrowerPlaceholder, itemPlaceholder, subItemPlaceholder, reasonPlaceholder)

    companion object {
        val borrowerPlaceholder = "borrower"
    }
}
