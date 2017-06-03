package com.gatehill.corebot.chat

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.ChatSettings
import com.ullink.slack.simpleslackapi.SlackPersona
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Handles Slack conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class SlackChatServiceImpl @Inject constructor(private val sessionService: SlackSessionService,
                                                    private val messageService: MessageService) : ChatService {

    private val logger: Logger = LogManager.getLogger(SlackChatServiceImpl::class.java)

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
            val messageContent = event.messageContent.trim()
            val splitCmd = messageContent.split("\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?".toRegex()).filterNot(String::isBlank)

            if (splitCmd.isNotEmpty() && isAddressedToBot(session.sessionPersona(), splitCmd[0])) {

                // skip element 0, which contains the bot's username
                val commandOnly = splitCmd.subList(1, splitCmd.size)

                messageService.handleMessage(commandOnly, trigger)
            }

        } catch (e: Exception) {
            logger.error("Error parsing message event: $event", e)
            session.addReactionToMessage(event.channel, event.timeStamp, "x")
            messageService.printUsage(trigger)
            return@SlackMessagePostedListener
        }
    })
}

/**
 * Determine if the message is addressed to the bot.
 */
fun isAddressedToBot(botPersona: SlackPersona, firstToken: String) =
        firstToken == "<@${botPersona.id}>" || firstToken == "<@${botPersona.id}>:"
