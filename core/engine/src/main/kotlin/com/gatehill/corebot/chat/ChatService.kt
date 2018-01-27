package com.gatehill.corebot.chat

/**
 * Handles conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ChatService {
    val supportsUserTermination: Boolean
    fun listenForEvents()
    fun stopListening()
}
