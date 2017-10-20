package com.gatehill.corebot.backend.items.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.backend.items.action.factory.BorrowItemAsUserFactory
import com.gatehill.corebot.backend.items.action.factory.BorrowItemFactory
import com.gatehill.corebot.backend.items.action.factory.EvictUserFromItemFactory
import com.gatehill.corebot.backend.items.config.ItemSettings
import com.gatehill.corebot.backend.items.config.OwnerDisplayMode
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

    fun claimItem(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>, triggerMessageSenderId: String) {
        val itemName = action.name

        synchronized(itemName) {
            checkItemClaims(action) { claims ->
                val reason: String = args[BorrowItemFactory.reasonPlaceholder]!!
                val subItem: String? = args[BorrowItemFactory.subItemPlaceholder]

                // check if on behalf of another user
                val borrower = args[BorrowItemAsUserFactory.borrowerPlaceholder] ?: triggerMessageSenderId

                itemClaims[itemName] = ItemClaims(claims.toMutableList().apply {

                    // any existing claim will be replaced
                    claims.firstOrNull { it.owner == borrower }?.let { existing ->
                        remove(existing)
                    }

                    add(ItemClaim(borrower, reason, subItem))
                })

                val borrowerDescription = if (borrower == triggerMessageSenderId) " <@$borrower>, you've" else ", <@$borrower> has"
                completeWithStatusCheck(trigger, future, "${chatGenerator.confirmation()}$borrowerDescription borrowed :lock: *$itemName* for _${reason}_.")
            }
        }
    }

    fun releaseItem(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig, triggerMessageSenderId: String) {
        val itemName = action.name

        synchronized(itemName) {
            checkItemClaims(action) { claims ->

                // check for existing claim for current user
                claims.firstOrNull { it.owner == triggerMessageSenderId }?.let { claim ->
                    itemClaims[itemName] = ItemClaims(claims.toMutableList().apply {
                        remove(claim)
                    })
                    completeWithStatusCheck(trigger, future, "${chatGenerator.confirmation()} <@$triggerMessageSenderId>, you've returned *$itemName*.")

                } ?: run {
                    completeWithStatusCheck(trigger, future, "BTW <@$triggerMessageSenderId>, you weren't borrowing *$itemName* :wink:")
                }
            }
        }
    }

    /**
     * Evict all borrowers from an item.
     */
    fun evictItemClaims(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig, triggerMessageSenderId: String) {
        val itemName = action.name

        synchronized(itemName) {
            checkItemClaims(action) { claims ->
                itemClaims.remove(itemName)

                val previousBorrowers = claims
                        .map { it.owner }
                        .filter { it != triggerMessageSenderId }
                        .let { nonSelfBorrowers ->
                            if (nonSelfBorrowers.isEmpty()) {
                                ""
                            } else {
                                "\n_(FYI ${nonSelfBorrowers.map { "<@$it>" }.joinToString()})_"
                            }
                        }

                completeWithStatusCheck(trigger, future, "${chatGenerator.confirmation()} <@$triggerMessageSenderId>, I've :unlock: evicted all borrowers (${claims.size}) from *$itemName*.$previousBorrowers")
            }
        }
    }

    /**
     * Evict a single borrower from an item.
     */
    fun evictUserFromItem(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig, args: Map<String, String>, triggerMessageSenderId: String) {
        val itemName = action.name

        synchronized(itemName) {
            checkItemClaims(action) { claims ->
                val borrower = args[EvictUserFromItemFactory.borrowerPlaceholder]!!

                when (claims.size) {
                    1 -> itemClaims.remove(itemName)
                    else -> itemClaims[itemName] = ItemClaims(claims.toMutableList().apply {
                        claims.firstOrNull { it.owner == borrower }?.let { existing ->
                            remove(existing)
                        }
                    })
                }

                completeWithStatusCheck(trigger, future, "${chatGenerator.confirmation()} <@$triggerMessageSenderId>, I've :unlock: evicted <@$borrower> from *$itemName*.")
            }
        }
    }

    /**
     * Complete with the result message, optionally printing the current status.
     */
    private fun completeWithStatusCheck(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, message: String) {
        val finalMessage = if (ItemSettings.showStatusOnChange) {
            "$message ${buildAllItemStatus(trigger)}"
        } else {
            message
        }

        future.complete(PerformActionResult(finalMessage))
    }

    fun checkItemClaims(itemName: String, callback: (List<ItemClaim>) -> Unit) =
            callback(itemClaims[itemName]?.claims ?: emptyList())

    fun checkItemClaims(action: ActionConfig, callback: (List<ItemClaim>) -> Unit) =
            checkItemClaims(action.name, callback)

    fun checkItemStatus(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        describeItem(trigger, action.name) {
            future.complete(PerformActionResult(it))
        }
    }

    fun describeItem(trigger: TriggerContext, itemName: String, callback: (String) -> Unit) {
        checkItemClaims(itemName) { claims ->
            val message = when {
                claims.isEmpty() -> "No one is borrowing *$itemName*."
                claims.size == 1 -> {
                    val claim = claims.first()
                    val subItemDescription = claim.subItem?.let { if (it.isNotBlank()) " ($it)" else null } ?: ""

                    "There is a single borrower of *$itemName*$subItemDescription: ${describeOwner(trigger, claim.owner)} - ${claim.reason}"
                }
                else -> {
                    val claimsList = StringBuilder()
                    claims.forEach { (owner, reason, subItem) ->
                        val subItemDescription = subItem?.let { if (it.isNotBlank()) "$it: " else null } ?: ""
                        claimsList.append("\n${" ".repeat(4)}• $subItemDescription${describeOwner(trigger, owner)} - $reason")
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
    private fun describeOwner(trigger: TriggerContext, owner: String) = when (ItemSettings.ownerDisplayMode) {
        OwnerDisplayMode.USERNAME -> sessionService.lookupUsername(trigger, owner)
        OwnerDisplayMode.REAL_NAME -> sessionService.lookupUserRealName(trigger, owner)
    }

    fun describeAllItemStatus(trigger: TriggerContext) = "${chatGenerator.greeting()} :simple_smile: ${buildAllItemStatus(trigger)}"

    private fun buildAllItemStatus(trigger: TriggerContext): String {
        val status = StringBuilder()

        configService.actions().keys.forEach { itemName ->
            describeItem(trigger, itemName) {
                if (status.isNotEmpty()) {
                    status.append("\n")
                }
                status.append(it)
            }
        }

        return "Here's the latest:\n$status"
    }
}
