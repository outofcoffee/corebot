package com.gatehill.corebot.chat

import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.ChatSettings
import com.ullink.slack.simpleslackapi.SlackPreparedMessage
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class SlackSessionServiceImpl @Inject constructor(configService: ConfigService) : SlackSessionService {
    private val logger: Logger = LogManager.getLogger(SlackSessionServiceImpl::class.java)

    override val session: SlackSession by lazy {
        logger.info("Connecting to Slack...")

        val session = SlackSessionFactory.createWebSocketSlackSession(ChatSettings.chat.authToken)
        connectedListeners.forEach { session.addSlackConnectedListener(it) }
        session.connect()

        logger.info("Connected to Slack [persona=${session.sessionPersona().userName}]")
        session
    }

    /**
     * Allow subclasses to hook into Slack events.
     */
    protected open val connectedListeners = listOf(SlackConnectedListener { _, theSession ->
        ChatSettings.chat.channelNames.forEach {
            val joinMessage = configService.joinMessage ?:
                    "${ChatLines.greeting()} :simple_smile: ${ChatLines.ready()}."

            theSession.findChannelByName(it)?.let { channel -> theSession.sendMessage(channel, joinMessage) }
                    ?: logger.warn("Unable to find channel: $it")
        }
    })

    override val botUsername: String
        get() = session.sessionPersona().userName

    override fun sendMessage(channelId: String, threadTimestamp: String, message: String) {
        if(threadTimestamp == ""){
            session.sendMessage(session.findChannelById(channelId), message)
        }else{
            val preparedMessage = SlackPreparedMessage.Builder()
                    .withMessage(message)
                    .withUnfurl(true)
                    .withThreadTimestamp(threadTimestamp)
                    .build()
            session.sendMessage(session.findChannelById(channelId), preparedMessage)
        }

    }

    override fun addReaction(channelId: String, messageTimestamp: String, emojiCode: String) {
        session.addReactionToMessage(session.findChannelById(channelId), messageTimestamp, emojiCode)
    }
}
