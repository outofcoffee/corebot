package com.gatehill.corebot.chat

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ChatGenerator {
    fun pleaseWait(): String
    fun goodNews(): String
    fun goodNewsEmoji(): String
    fun badNews(): String
    fun badNewsEmoji(): String
    fun greeting(): String
    fun ready(): String
    fun confirmation(): String
}
