package com.gatehill.corebot.driver.items.action.factory

import com.gatehill.corebot.action.factory.ActionMessageMode
import com.gatehill.corebot.action.factory.SystemActionFactory
import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import com.gatehill.corebot.driver.items.service.ClaimService
import javax.inject.Inject

/**
 * Show status and claims for all items.
 */
@Template("statusAllItems", builtIn = true, showInUsage = true, actionMessageMode = ActionMessageMode.INDIVIDUAL)
class StatusAllFactory @Inject constructor(val claimService: ClaimService) : SystemActionFactory() {
    override val actionType: ActionType = ItemsActionType.ALL_STATUS

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) =
            claimService.describeAllItemStatus(trigger)
}
