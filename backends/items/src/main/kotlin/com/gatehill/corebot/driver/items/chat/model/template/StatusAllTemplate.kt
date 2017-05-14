package com.gatehill.corebot.driver.items.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.template.ActionMessageMode
import com.gatehill.corebot.chat.model.template.SystemActionTemplate
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import com.gatehill.corebot.driver.items.service.ClaimService
import java.util.*
import javax.inject.Inject

/**
 * Show status and claims for all items.
 */
class StatusAllTemplate @Inject constructor(val configService: ConfigService,
                                            val claimService: ClaimService) : SystemActionTemplate() {

    override val builtIn = false
    override val showInUsage = true
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
    override val actionType: ActionType = ItemsActionType.ALL_STATUS
    override val tokens = LinkedList(listOf("status"))

    override fun buildStartMessage(options: Map<String, String>, actionConfig: ActionConfig?): String {
        val status = StringBuilder()

        configService.actions().keys.forEach { itemName ->
            claimService.describeItem(itemName) {
                if (status.isNotEmpty()) {
                    status.append("\n")
                }
                status.append(it)
            }
        }

        return "Here's the latest:\n$status"
    }
}
