package com.gatehill.corebot.chat

import com.gatehill.corebot.operation.model.TriggerContext

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface SessionService {
    val botUsername: String
    fun sendMessage(trigger: TriggerContext, message: String)
    fun addReaction(trigger: TriggerContext, emojiCode: String)
    fun lookupUsername(trigger: TriggerContext, userId: String): String
    fun lookupUserRealName(trigger: TriggerContext, userId: String): String
}
