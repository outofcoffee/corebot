package com.gatehill.corebot.driver.items.action.model

import com.gatehill.corebot.chat.model.action.CoreActionType

class ItemsActionType(name: String, description: String) : CoreActionType(name, description) {
    companion object {
        val ITEM_BORROW = ItemsActionType("ITEM_BORROW", "Borrow an item")
        val ITEM_BORROW_AS_USER = ItemsActionType("ITEM_BORROW_AS_USER", "Borrow an item as another user")
        val ITEM_RETURN = ItemsActionType("ITEM_RETURN", "Return an item")
        val ITEM_EVICT = ItemsActionType("ITEM_EVICT", "Evict all borrowers from an item")
        val ITEM_EVICT_USER = ItemsActionType("ITEM_EVICT_USER", "Evict a user from an item")
        val ITEM_STATUS = ItemsActionType("ITEM_STATUS", "Check status of an item")
        val ALL_STATUS = ItemsActionType("ALL_STATUS", "Check status of all items")
    }
}
