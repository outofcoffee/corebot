package com.gatehill.corebot.backend.items.action.factory

import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.PlainOperationFactory
import com.gatehill.corebot.operation.factory.Template
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.backend.items.action.model.ItemsOperationType
import com.gatehill.corebot.backend.items.service.ClaimService
import javax.inject.Inject

/**
 * Show status and claims for all items.
 */
@Template("statusAllItems", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class StatusAllFactory @Inject constructor(private val claimService: ClaimService) : PlainOperationFactory() {
    override val operationType: OperationType = ItemsOperationType.ALL_STATUS

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) =
            claimService.describeAllItemStatus(trigger)
}
