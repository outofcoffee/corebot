package com.gatehill.corebot.frontend.slack.chat

import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.chat.MessageService
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.frontend.slack.config.ChatSettings
import com.google.common.cache.CacheBuilder
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit
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

    /**
     * Caches message IDs for de-duplication purposes.
     * This is a work-around for a Slack library bug: [https://github.com/Ullink/simple-slack-api/issues/180]
     */
    private val messageIdCache = CacheBuilder.newBuilder()
            .expireAfterAccess(ChatSettings.chat.messageIdCache, TimeUnit.SECONDS)
            .build<String, String>()

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
                .map { channelName -> session.findChannelByName(channelName)?.id }
                .filterNotNull()
                .contains(event.channel.id)) return@SlackMessagePostedListener

        // ignore own messages
        if (session.sessionPersona().id == event.sender.id) return@SlackMessagePostedListener

        // filter duplicates
        if (isDuplicate(event, session)) return@SlackMessagePostedListener

        val trigger = TriggerContext(event.channel.id, event.sender.id, event.sender.userName, event.timestamp, event.threadTimestamp)

        try {
            // some message events have null content
            event.messageContent?.trim()?.let { messageContent ->
                executeBotAction(session, messageContent) { command ->
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

    /**
     * Determine whether this message has been processed already.
     */
    private fun isDuplicate(event: SlackMessagePosted, session: SlackSession): Boolean {
        // According to https://api.slack.com/events-api
        // 'The combination of event_ts, team_id, user_id, or channel_id is
        // intended to be unique. This field is included with every inner event type'.
        val uniqueId = "${event.timeStamp}_${session.team.id}_${event.sender?.id}_${event.channel?.id}"

        synchronized(messageIdCache) {
            val found = (uniqueId == messageIdCache.getIfPresent(uniqueId))
            if (found) {
                logger.debug("Duplicate message received: $event")
            } else {
                messageIdCache.put(uniqueId, uniqueId)
            }
            return found
        }
    }

    /***
     * Execute the `block` if the message is addressed to the bot and return its result.
     */
    protected fun <T> executeBotAction(session: SlackSession, messageContent: String, block: (command: String?) -> T) {
        val matcher = messageMatcher.matcher(messageContent)

        matcher.takeIf { it.matches() && it.group("botUser") == session.sessionPersona().id }?.run {
            block(try {
                matcher.group("command")
            } catch (e: IllegalStateException) {
                null
            })
        }
    }
}
