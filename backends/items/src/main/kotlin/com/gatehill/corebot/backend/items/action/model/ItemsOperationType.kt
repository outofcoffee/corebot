package com.gatehill.corebot.backend.items.action.model

import com.gatehill.corebot.operation.model.CoreOperationType

class ItemsOperationType(name: String, description: String) : CoreOperationType(name, description) {
    companion object {
        val ITEM_BORROW = ItemsOperationType("ITEM_BORROW", "Borrow an item")
        val ITEM_BORROW_AS_USER = ItemsOperationType("ITEM_BORROW_AS_USER", "Borrow an item as another user")
        val ITEM_RETURN = ItemsOperationType("ITEM_RETURN", "Return an item")
        val ITEM_EVICT = ItemsOperationType("ITEM_EVICT", "Evict all borrowers from an item")
        val ITEM_EVICT_USER = ItemsOperationType("ITEM_EVICT_USER", "Evict a user from an item")
        val ITEM_STATUS = ItemsOperationType("ITEM_STATUS", "Check status of an item")
        val ALL_STATUS = ItemsOperationType("ALL_STATUS", "Check status of all items")
    }
}
