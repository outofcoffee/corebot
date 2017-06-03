package com.gatehill.corebot.chat

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.ChatSettings
import com.gatehill.corebot.config.ConfigService
import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackPreparedMessage
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class SlackSessionServiceImpl @Inject constructor(configService: ConfigService,
                                                       private val chatGenerator: ChatGenerator) : SlackSessionService {

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
                    "${chatGenerator.greeting()} :simple_smile: ${chatGenerator.ready()}."

            theSession.findChannelByName(it)?.let { channel -> theSession.sendMessage(channel, joinMessage) }
                    ?: logger.warn("Unable to find channel: $it")
        }
    })

    override val botUsername: String
        get() = session.sessionPersona().userName

    override fun sendMessage(triggerContext: TriggerContext, message: String) {
        sendMessage(session.findChannelById(triggerContext.channelId), triggerContext.messageTimestamp,
                triggerContext.messageThreadTimestamp, message)
    }

    override fun sendMessage(event: SlackMessagePosted, message: String) {
        sendMessage(event.channel, event.timestamp, event.threadTimestamp, message)
    }

    private fun sendMessage(channel: SlackChannel, triggerMessageTimestamp: String,
                            triggerMessageThreadTimestamp: String?, message: String) {

        val messageBuilder = SlackPreparedMessage.Builder().apply {
            withMessage(message)
            withUnfurl(true)

            // the 'replyInThread' setting implies 'allowThreadedTriggers'
            if (ChatSettings.threads.replyInThread || ChatSettings.threads.allowThreadedTriggers) {
                // according to: https://api.slack.com/docs/message-threading
                // using the message timestamp is enough to attach a reply to a thread
                val timestamp = if (ChatSettings.threads.allowThreadedTriggers) {
                    triggerMessageThreadTimestamp ?: triggerMessageTimestamp
                } else {
                    triggerMessageTimestamp
                }

                withThreadTimestamp(timestamp)
            }
        }

        session.sendMessage(channel, messageBuilder.build())
    }

    override fun addReaction(triggerContext: TriggerContext, emojiCode: String) {
        session.addReactionToMessage(session.findChannelById(triggerContext.channelId), triggerContext.messageTimestamp, emojiCode)
    }

    override fun lookupUsername(userId: String): String =
            session.findUserById(userId).userName

    override fun lookupUserRealName(userId: String): String =
            session.findUserById(userId).realName
}
