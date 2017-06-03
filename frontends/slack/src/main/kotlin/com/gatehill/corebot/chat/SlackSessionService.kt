package com.gatehill.corebot.chat

import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface SlackSessionService : SessionService {
    val session: SlackSession

    fun sendMessage(event: SlackMessagePosted, message: String)
}
