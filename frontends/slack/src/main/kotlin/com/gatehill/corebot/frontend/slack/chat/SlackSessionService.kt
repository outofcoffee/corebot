package com.gatehill.corebot.frontend.slack.chat

import com.gatehill.corebot.chat.SessionService
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface SlackSessionService : SessionService {
    val session: SlackSession

    fun sendMessage(event: SlackMessagePosted, message: String)
}
