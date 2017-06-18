package com.gatehill.corebot.backend.items.action

import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.ActionDriver
import com.gatehill.corebot.backend.items.action.model.ItemsOperationType
import com.gatehill.corebot.backend.items.service.ClaimService
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ItemsActionDriverImpl @Inject constructor(private val claimService: ClaimService) : ActionDriver {
    override fun perform(trigger: TriggerContext, operationType: OperationType, action: ActionConfig, args: Map<String, String>): CompletableFuture<PerformActionResult> {
        val future = CompletableFuture<PerformActionResult>()
        try {
            when (operationType) {
                ItemsOperationType.ITEM_EVICT_USER -> claimService.evictUserFromItem(trigger, future, action, args, trigger.userId)
                ItemsOperationType.ITEM_BORROW -> claimService.claimItem(trigger, future, action, args, trigger.userId)
                ItemsOperationType.ITEM_BORROW_AS_USER -> claimService.claimItem(trigger, future, action, args, trigger.userId)
                ItemsOperationType.ITEM_RETURN -> claimService.releaseItem(trigger, future, action, trigger.userId)
                ItemsOperationType.ITEM_EVICT -> claimService.evictItemClaims(trigger, future, action, trigger.userId)
                ItemsOperationType.ITEM_STATUS -> claimService.checkItemStatus(trigger, future, action)
                else -> throw UnsupportedOperationException("Operation type $operationType is not supported by ${javaClass.canonicalName}")
            }
        } catch (e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }
}
