package com.gatehill.corebot.backend.items.action.factory

import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.Template
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.backend.items.action.model.ItemsOperationType
import javax.inject.Inject

/**
 * Borrow an item as another user.
 */
@Template("borrowItemAsUser", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL,
        placeholderKeys = arrayOf(
                BorrowItemAsUserFactory.borrowerPlaceholder,
                BaseItemFactory.itemPlaceholder,
                BorrowItemFactory.subItemPlaceholder,
                BorrowItemFactory.reasonPlaceholder
        ))
class BorrowItemAsUserFactory @Inject constructor(configService: ConfigService) : BorrowItemFactory(configService) {
    override val operationType: OperationType = ItemsOperationType.ITEM_BORROW_AS_USER

    companion object {
        const val borrowerPlaceholder = "borrower"
    }
}
