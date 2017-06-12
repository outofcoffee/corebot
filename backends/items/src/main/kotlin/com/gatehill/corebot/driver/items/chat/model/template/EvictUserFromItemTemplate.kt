package com.gatehill.corebot.driver.items.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import java.util.LinkedList
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Evict a borrower from an item.
 */
class EvictUserFromItemTemplate @Inject constructor(configService: ConfigService) : BorrowItemTemplate(configService) {
    override val actionType: ActionType = ItemsActionType.ITEM_EVICT_USER
    override val tokens = LinkedList(listOf("evict", "{$borrowerPlaceholder}", "from", "{$itemPlaceholder}"))
    override val templateRegex: Pattern?
        get() = "evict\\s\\<@(?<$borrowerPlaceholder>[a-zA-Z0-9]+)\\>\\sfrom\\s+(?<$itemPlaceholder>[a-zA-Z0-9]+)".toPattern()

    companion object {
        val borrowerPlaceholder = "borrower"
    }
}
