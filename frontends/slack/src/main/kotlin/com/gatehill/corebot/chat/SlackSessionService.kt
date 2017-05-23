package com.gatehill.corebot.chat

import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackSession

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface SlackSessionService : SessionService {
    val session: SlackSession

    fun sendMessage(channel: SlackChannel, triggerMessageTimestamp: String, message: String)
}
