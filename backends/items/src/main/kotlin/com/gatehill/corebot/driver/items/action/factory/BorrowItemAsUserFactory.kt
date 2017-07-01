package com.gatehill.corebot.driver.items.action.factory

import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import javax.inject.Inject

/**
 * Borrow an item as another user.
 */
@Template("borrowItemAsUser", placeholderKeys = arrayOf(
        BorrowItemAsUserFactory.borrowerPlaceholder,
        BaseItemFactory.itemPlaceholder,
        BorrowItemFactory.subItemPlaceholder,
        BorrowItemFactory.reasonPlaceholder
))
class BorrowItemAsUserFactory @Inject constructor(configService: ConfigService) : BorrowItemFactory(configService) {
    override val actionType: ActionType = ItemsActionType.ITEM_BORROW_AS_USER

    companion object {
        const val borrowerPlaceholder = "borrower"
    }
}
