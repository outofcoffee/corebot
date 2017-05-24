package com.gatehill.corebot.chat

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface SessionService {
    val botUsername: String
    fun sendMessage(channelId: String, triggerMessageTimestamp: String, message: String)
    fun addReaction(channelId: String, triggerMessageTimestamp: String, emojiCode: String)
}
