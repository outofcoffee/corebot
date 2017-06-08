package com.gatehill.corebot.driver.items.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import java.util.LinkedList
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Borrow an item as another user.
 */
class BorrowItemAsUserTemplate @Inject constructor(configService: ConfigService) : BorrowItemTemplate(configService) {
    override val actionType: ActionType = ItemsActionType.ITEM_BORROW_AS_USER
    override val tokens = LinkedList(listOf("as", "{$borrowerPlaceholder}", "borrow", "{$itemPlaceholder}", "{$subItemPlaceholder}", "for", "{$reasonPlaceholder}"))
    override val templateRegex: Pattern?
        get() = "as\\s\\<@(?<$borrowerPlaceholder>[a-zA-Z0-9]+)\\>\\sborrow\\s+(?<$itemPlaceholder>[a-zA-Z0-9]+)\\s*(?<optionalSubItemName>.*)\\s+for\\s+(?<reason>.+)".toPattern()

    companion object {
        val borrowerPlaceholder = "borrower"
    }
}
