package com.gatehill.corebot.driver.items.service

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.chat.model.template.BorrowItemTemplate
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * Allows an item to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ClaimService @Inject constructor(private val sessionService: SessionService) {
    /**
     * A claim held on an item.
     */
    data class ItemClaim(val owner: String, val reason: String)

    private val itemClaims = mutableMapOf<String, List<ItemClaim>>()

    fun claimItem(future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>,
                  triggerMessageSenderId: String) {

        val itemName = action.name
        synchronized(itemName) {
            checkItemClaims(action) { claims ->
                val reason = args[BorrowItemTemplate.reasonPlaceholder]!!

                itemClaims[itemName] = claims.toMutableList().apply {

                    // any existing claim will be replaced
                    claims.firstOrNull { it.owner == triggerMessageSenderId }?.let { existing ->
                        remove(existing)
                    }

                    add(ItemClaim(triggerMessageSenderId, reason))
                }

                future.complete(PerformActionResult("You've borrowed :lock: *$itemName* for _${reason}_."))
            }
        }
    }

    fun releaseItem(future: CompletableFuture<PerformActionResult>, action: ActionConfig,
                    triggerMessageSenderId: String) {

        val itemName = action.name
        synchronized(itemName) {
            checkItemClaims(action) { claims ->

                // check for existing claim for current user
                claims.firstOrNull { it.owner == triggerMessageSenderId }?.let { claim ->
                    itemClaims[itemName] = claims.toMutableList().apply {
                        remove(claim)
                    }

                    future.complete(PerformActionResult("You've returned *$itemName*."))

                } ?: run {
                    future.complete(PerformActionResult("BTW, you weren't borrowing *$itemName* :wink:"))
                }
            }
        }
    }

    fun evictItemClaims(future: CompletableFuture<PerformActionResult>, action: ActionConfig,
                        triggerMessageSenderId: String) {

        val itemName = action.name
        synchronized(itemName) {
            checkItemClaims(action) { claims ->
                itemClaims[itemName] = emptyList()

                val nonSelfBorrowers = claims
                        .map{ it.owner }
                        .filter { it != triggerMessageSenderId }

                val previousBorrowers = if (nonSelfBorrowers.isEmpty()) {
                    ""
                } else {
                    "\n_(FYI ${nonSelfBorrowers.map { "<@$it>" }.joinToString()})_"
                }

                val message = "I've :unlock: evicted all borrowers (${claims.size}) from *$itemName*.$previousBorrowers"
                future.complete(PerformActionResult(message))
            }
        }
    }

    fun checkItemClaims(itemName: String, callback: (List<ClaimService.ItemClaim>) -> Unit) =
            callback(itemClaims[itemName] ?: emptyList())

    fun checkItemClaims(action: ActionConfig, callback: (List<ClaimService.ItemClaim>) -> Unit) =
            checkItemClaims(action.name, callback)

    fun checkItemStatus(future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        describeItem(action.name) {
            future.complete(PerformActionResult(it))
        }
    }

    fun describeItem(itemName: String, callback: (String) -> Unit) {
        checkItemClaims(itemName) { claims ->
            val message = when {
                claims.isEmpty() -> "No one is borrowing *$itemName*."
                claims.size == 1 -> {
                    val claim = claims.first()
                    val ownerUsername = sessionService.lookupUser(claim.owner)
                    "There is a single borrower of *$itemName*: $ownerUsername - ${claim.reason}"
                }
                else -> {
                    val claimsList = StringBuilder()
                    claims.forEach { (owner, reason) ->
                        val ownerUsername = sessionService.lookupUser(owner)
                        claimsList.append("\n• $ownerUsername - $reason")
                    }
                    "There are ${claims.size} borrowers of *$itemName*:$claimsList"
                }
            }

            callback(message)
        }
    }
}
