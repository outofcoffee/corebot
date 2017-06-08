package com.gatehill.corebot.driver.items.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.items.chat.model.template.BorrowItemAsUserTemplate
import com.gatehill.corebot.driver.items.chat.model.template.BorrowItemTemplate
import com.gatehill.corebot.driver.items.config.OwnerDisplayMode
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
class ClaimService @Inject constructor(private val configService: ConfigService,
                                       private val sessionService: SessionService,
                                       private val chatGenerator: ChatGenerator,
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

    fun claimItem(future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>, triggerMessageSenderId: String) {
        val itemName = action.name

        synchronized(itemName) {
            checkItemClaims(action) { claims ->
                val reason: String = args[BorrowItemTemplate.reasonPlaceholder]!!
                val subItem: String? = args[BorrowItemTemplate.subItemPlaceholder]

                // check if on behalf of another user
                val borrower = args[BorrowItemAsUserTemplate.borrower] ?: triggerMessageSenderId

                itemClaims[itemName] = ItemClaims(claims.toMutableList().apply {

                    // any existing claim will be replaced
                    claims.firstOrNull { it.owner == borrower }?.let { existing ->
                        remove(existing)
                    }

                    add(ItemClaim(borrower, reason, subItem))
                })

                val borrowerDescription = if (borrower == triggerMessageSenderId) " <@$borrower>, you've" else ", <@$borrower> has"
                completeWithStatusCheck(future, "OK$borrowerDescription borrowed :lock: *$itemName* for _${reason}_.")
            }
        }
    }

    fun releaseItem(future: CompletableFuture<PerformActionResult>, action: ActionConfig, triggerMessageSenderId: String) {
        val itemName = action.name

        synchronized(itemName) {
            checkItemClaims(action) { claims ->

                // check for existing claim for current user
                claims.firstOrNull { it.owner == triggerMessageSenderId }?.let { claim ->
                    itemClaims[itemName] = ItemClaims(claims.toMutableList().apply {
                        remove(claim)
                    })
                    completeWithStatusCheck(future, "OK <@$triggerMessageSenderId>, you've returned *$itemName*.")

                } ?: run {
                    completeWithStatusCheck(future, "BTW <@$triggerMessageSenderId>, you weren't borrowing *$itemName* :wink:")
                }
            }
        }
    }

    fun evictItemClaims(future: CompletableFuture<PerformActionResult>, action: ActionConfig, triggerMessageSenderId: String) {
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

                completeWithStatusCheck(future, "OK <@$triggerMessageSenderId>, I've :unlock: evicted all borrowers (${claims.size}) from *$itemName*.$previousBorrowers")
            }
        }
    }

    /**
     * Complete with the result message, optionally printing the current status.
     */
    private fun completeWithStatusCheck(future: CompletableFuture<PerformActionResult>, message: String) {
        val finalMessage = if (ItemSettings.showStatusOnChange) {
            "$message ${buildAllItemStatus()}"
        } else {
            message
        }

        future.complete(PerformActionResult(finalMessage))
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
                    val subItemDescription = claim.subItem?.let { if (it.isNotBlank()) " ($it)" else null } ?: ""

                    "There is a single borrower of *$itemName*$subItemDescription: ${describeOwner(claim.owner)} - ${claim.reason}"
                }
                else -> {
                    val claimsList = StringBuilder()
                    claims.forEach { (owner, reason, subItem) ->
                        val subItemDescription = subItem?.let { if (it.isNotBlank()) "$it: " else null } ?: ""
                        claimsList.append("\n    • $subItemDescription${describeOwner(owner)} - $reason")
                    }

                    "There are ${claims.size} borrowers of *$itemName*:$claimsList"
                }
            }

            callback(message)
        }
    }

    /**
     * Describe the owner, based on the display mode.
     */
    private fun describeOwner(owner: String) = when (ItemSettings.ownerDisplayMode) {
        OwnerDisplayMode.USERNAME -> sessionService.lookupUsername(owner)
        OwnerDisplayMode.REAL_NAME -> sessionService.lookupUserRealName(owner)
    }

    fun describeAllItemStatus() = "${chatGenerator.greeting()} :simple_smile: ${buildAllItemStatus()}"

    private fun buildAllItemStatus(): String {
        val status = StringBuilder()

        configService.actions().keys.forEach { itemName ->
            describeItem(itemName) {
                if (status.isNotEmpty()) {
                    status.append("\n")
                }
                status.append(it)
            }
        }

        return "Here's the latest:\n$status"
    }
}
