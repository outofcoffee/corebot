package com.gatehill.corebot.chat

import com.gatehill.corebot.config.Settings
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackSessionServiceImpl : SlackSessionService {
    override val session: SlackSession by lazy {
        val session = SlackSessionFactory.createWebSocketSlackSession(Settings.chat.authToken)
        session.connect()
        session
    }

    override val botUsername: String
        get() = session.sessionPersona().userName

    override fun sendMessage(channelId: String, message: String) {
        session.sendMessage(session.findChannelById(channelId), message)
    }

    override fun addReaction(channelId: String, messageTimestamp: String, emojiCode: String) {
        session.addReactionToMessage(session.findChannelById(channelId), messageTimestamp, emojiCode)
    }
}
