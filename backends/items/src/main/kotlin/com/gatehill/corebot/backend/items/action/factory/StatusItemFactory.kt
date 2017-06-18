package com.gatehill.corebot.backend.items.action.factory

import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.Template
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.backend.items.action.model.ItemsOperationType
import javax.inject.Inject

/**
 * Show status and claims for an item.
 */
@Template("statusItem", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class StatusItemFactory @Inject constructor(configService: ConfigService) : BaseItemFactory(configService) {
    override val operationType: OperationType = ItemsOperationType.ITEM_STATUS
    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""
}
