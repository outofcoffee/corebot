package com.gatehill.corebot.driver.items.action

import com.gatehill.corebot.action.driver.ActionDriver
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.action.model.ItemsActionType
import com.gatehill.corebot.driver.items.service.ClaimService
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ItemsActionDriverImpl @Inject constructor(private val claimService: ClaimService) : ActionDriver {
    override fun perform(trigger: TriggerContext, actionType: ActionType, action: ActionConfig, args: Map<String, String>): CompletableFuture<PerformActionResult> {
        val future = CompletableFuture<PerformActionResult>()
        try {
            when (actionType) {
                ItemsActionType.ITEM_EVICT_USER -> claimService.evictUserFromItem(trigger, future, action, args, trigger.userId)
                ItemsActionType.ITEM_BORROW -> claimService.claimItem(trigger, future, action, args, trigger.userId)
                ItemsActionType.ITEM_BORROW_AS_USER -> claimService.claimItem(trigger, future, action, args, trigger.userId)
                ItemsActionType.ITEM_RETURN -> claimService.releaseItem(trigger, future, action, trigger.userId)
                ItemsActionType.ITEM_EVICT -> claimService.evictItemClaims(trigger, future, action, trigger.userId)
                ItemsActionType.ITEM_STATUS -> claimService.checkItemStatus(trigger, future, action)
                else -> throw UnsupportedOperationException("Action type $actionType is not supported by ${javaClass.canonicalName}")
            }
        } catch (e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }
}
