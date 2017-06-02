package com.gatehill.corebot.driver.items.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.chat.model.template.BorrowItemTemplate
import com.gatehill.corebot.driver.items.config.ItemSettings
import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.partition
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Named

/**
 * Allows an item to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ClaimService @Inject constructor(private val sessionService: SessionService,
                                       @Named("itemStore") private val dataStore: DataStore) {

    /**
     * A claim held on an item.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ItemClaim(val owner: String,
                         val reason: String,
                         val subItem: String?)

    data class ItemClaims(val claims: List<ItemClaim>)

    private val itemClaims
        get() = dataStore.partition<String, ItemClaims>("itemClaims")

    fun claimItem(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>,
                  triggerMessageSenderId: String) {

        val itemName = action.name
        synchronized(itemName) {
            checkItemClaims(action) { claims ->
                val reason: String = args[BorrowItemTemplate.reasonPlaceholder]!!
                val subItem: String? = args[BorrowItemTemplate.subItemPlaceholder]

                itemClaims[itemName] = ItemClaims(claims.toMutableList().apply {

                    // any existing claim will be replaced
                    claims.firstOrNull { it.owner == triggerMessageSenderId }?.let { existing ->
                        remove(existing)
                    }

                    add(ItemClaim(triggerMessageSenderId, reason, subItem))
                })

                completeWithStatusCheck(trigger, future, itemName,
                        "You've borrowed :lock: *$itemName* for _${reason}_.")
            }
        }
    }

    fun releaseItem(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig,
                    triggerMessageSenderId: String) {

        val itemName = action.name

        synchronized(itemName) {
            checkItemClaims(action) { claims ->

                // check for existing claim for current user
                claims.firstOrNull { it.owner == triggerMessageSenderId }?.let { claim ->
                    itemClaims[itemName] = ItemClaims(claims.toMutableList().apply {
                        remove(claim)
                    })
                    completeWithStatusCheck(trigger, future, itemName, "You've returned *$itemName*.")

                } ?: run {
                    completeWithStatusCheck(trigger, future, itemName, "BTW, you weren't borrowing *$itemName* :wink:")
                }
            }
        }
    }

    fun evictItemClaims(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig,
                        triggerMessageSenderId: String) {

        val itemName = action.name
        synchronized(itemName) {
            checkItemClaims(action) { claims ->
                itemClaims.remove(itemName)

                val nonSelfBorrowers = claims
                        .map { it.owner }
                        .filter { it != triggerMessageSenderId }

                val previousBorrowers = if (nonSelfBorrowers.isEmpty()) {
                    ""
                } else {
                    "\n_(FYI ${nonSelfBorrowers.map { "<@$it>" }.joinToString()})_"
                }

                completeWithStatusCheck(trigger, future, itemName,
                        "I've :unlock: evicted all borrowers (${claims.size}) from *$itemName*.$previousBorrowers")
            }
        }
    }

    /**
     * Follow up with status.
     */
    private fun completeWithStatusCheck(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                                        itemName: String, message: String) {

        future.complete(PerformActionResult(message))
        if (ItemSettings.showStatusOnChange) {
            describeItem(itemName) { sessionService.sendMessage(trigger.channelId, it) }
        }
    }

    fun checkItemClaims(itemName: String, callback: (List<ClaimService.ItemClaim>) -> Unit) =
            callback(itemClaims[itemName]?.claims ?: emptyList())

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
                    val subItemDescription = claim.subItem?.let { if (it.isNotBlank()) " ($it)" else null } ?: ""

                    "There is a single borrower of *$itemName*$subItemDescription: $ownerUsername - ${claim.reason}"
                }
                else -> {
                    val claimsList = StringBuilder()
                    claims.forEach { (owner, reason, subItem) ->
                        val ownerUsername = sessionService.lookupUser(owner)
                        val subItemDescription = subItem?.let { if (it.isNotBlank()) "$it: " else null } ?: ""

                        claimsList.append("\n• $subItemDescription$ownerUsername - $reason")
                    }
                    "There are ${claims.size} borrowers of *$itemName*:$claimsList"
                }
            }

            callback(message)
        }
    }
}
