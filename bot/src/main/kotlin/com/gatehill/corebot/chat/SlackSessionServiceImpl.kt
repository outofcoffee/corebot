package com.gatehill.corebot.chat

import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.Settings
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackSessionServiceImpl @Inject constructor(configService: ConfigService) : SlackSessionService {
    override val session: SlackSession by lazy {
        val session = SlackSessionFactory.createWebSocketSlackSession(Settings.chat.authToken)

        session.addSlackConnectedListener { slackConnected, theSession ->
            Settings.chat.channelNames.forEach {
                val joinMessage = configService.joinMessage ?:
                        "${ChatLines.greeting()} :simple_smile: ${ChatLines.ready()}."

                theSession.sendMessage(theSession.findChannelByName(it), joinMessage)
            }
        }

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
