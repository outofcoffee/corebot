package com.gatehill.corebot.chat

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface SessionService {
    val botUsername: String
    fun sendMessage(channelId: String, threadTimestamp: String, message: String)
    fun addReaction(channelId: String, messageTimestamp: String, emojiCode: String)
}
