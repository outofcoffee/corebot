package com.gatehill.corebot.driver.items.action.factory

import com.gatehill.corebot.action.factory.OperationMessageMode
import com.gatehill.corebot.action.factory.PlainOperationFactory
import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsOperationType
import com.gatehill.corebot.driver.items.service.ClaimService
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
