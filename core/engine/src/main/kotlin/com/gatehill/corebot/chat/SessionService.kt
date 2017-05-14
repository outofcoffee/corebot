package com.gatehill.corebot.chat

import com.gatehill.corebot.action.model.TriggerContext

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface SessionService {
    val botUsername: String
    fun sendMessage(triggerContext: TriggerContext, message: String)
    fun addReaction(triggerContext: TriggerContext, emojiCode: String)
    fun lookupUser(userId: String): String
}
