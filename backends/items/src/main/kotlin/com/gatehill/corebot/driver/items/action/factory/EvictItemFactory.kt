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
 * Evict all borrowers from an item.
 */
@Template("evictItem", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class EvictItemFactory @Inject constructor(configService: ConfigService) : BaseItemFactory(configService) {
    override val operationType: OperationType = ItemsOperationType.ITEM_EVICT
    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""
}
