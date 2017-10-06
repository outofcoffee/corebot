package com.gatehill.corebot.driver.items.action.factory

import com.gatehill.corebot.action.factory.OperationMessageMode
import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsOperationType
import javax.inject.Inject

/**
 * Evict a borrower from an item.
 */
@Template("evictUserFromItem", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL,
        placeholderKeys = arrayOf(
                EvictUserFromItemFactory.borrowerPlaceholder,
                BaseItemFactory.itemPlaceholder
        ))
class EvictUserFromItemFactory @Inject constructor(configService: ConfigService) : BaseItemFactory(configService) {
    override val operationType: OperationType = ItemsOperationType.ITEM_EVICT_USER

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""

    companion object {
        const val borrowerPlaceholder = "borrower"
    }
}
