package com.gatehill.corebot.chat

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.ChatSettings
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Handles Slack conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class SlackChatServiceImpl @Inject constructor(private val sessionService: SlackSessionService,
                                                    private val messageService: MessageService) : ChatService {

    private val logger: Logger = LogManager.getLogger(SlackChatServiceImpl::class.java)
    private val messageMatcher = Pattern.compile("<@(?<botUser>[a-zA-Z0-9]+)>:?(\\s(?<command>.+))?")

    override fun listenForEvents() {
        messagePostedListeners.forEach { sessionService.session.addMessagePostedListener(it) }
    }

    override fun stopListening() {
        sessionService.session.disconnect()
    }

    /**
     * Allow subclasses to hook into Slack events.
     */
    protected open val messagePostedListeners = listOf(SlackMessagePostedListener { event, session ->
        // filter out messages from other channels
        if (!ChatSettings.chat.channelNames
                .map { channelName -> session.findChannelByName(channelName).id }
                .contains(event.channel.id)) return@SlackMessagePostedListener

        // ignore own messages
        if (session.sessionPersona().id == event.sender.id) return@SlackMessagePostedListener

        val trigger = TriggerContext(event.channel.id, event.sender.id, event.sender.userName, event.timestamp, event.threadTimestamp)

        try {
            // some message events have null content
            event.messageContent?.trim()?.let { messageContent ->
                // determine whether message is addressed to the bot
                val matcher = messageMatcher.matcher(messageContent)
                matcher.takeIf { it.matches() }?.takeIf { it.group("botUser") == session.sessionPersona().id }?.run {

                    // look for a command token
                    val command = try {
                        matcher.group("command")
                    } catch (e: IllegalStateException) {
                        null
                    }

                    command?.let {
                        messageService.handleMessage(trigger, it)
                    } ?: run {
                        logger.warn("Ignoring malformed command '$messageContent' from ${trigger.username}")
                        messageService.handleUnknownCommand(trigger)
                    }
                }

            } ?: logger.trace("Ignoring event with null message: $event")

        } catch (e: Exception) {
            logger.error("Error parsing message event: $event", e)
            session.addReactionToMessage(event.channel, event.timeStamp, "x")
            messageService.printUsage(trigger)
            return@SlackMessagePostedListener
        }
    })
}
