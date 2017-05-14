package com.gatehill.corebot.chat

import com.gatehill.corebot.action.ActionPerformService
import com.gatehill.corebot.action.model.PerformActionRequest
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.Action
import com.gatehill.corebot.chat.model.action.ActionWrapper
import com.gatehill.corebot.chat.model.action.CustomAction
import com.gatehill.corebot.chat.model.template.ActionMessageMode
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.security.AuthorisationService
import com.ullink.slack.simpleslackapi.SlackPersona
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
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
                                                    private val templateService: TemplateService,
                                                    private val configService: ConfigService,
                                                    private val authorisationService: AuthorisationService,
                                                    private val actionPerformService: ActionPerformService) : ChatService {

    private val logger: Logger = LogManager.getLogger(SlackChatServiceImpl::class.java)

    override fun listenForEvents() {
        messagePostedListeners.forEach { sessionService.session.addMessagePostedListener(it) }
    }

    /**
     * Allow subclasses to hook into Slack events.
     */
    protected open val messagePostedListeners = listOf(SlackMessagePostedListener { event, session ->
        // filter out messages from other channels
        if (!Settings.chat.channelNames.map { channelName -> session.findChannelByName(channelName).id }
                .contains(event.channel.id)) return@SlackMessagePostedListener

        // ignore own messages
        if (session.sessionPersona().id == event.sender.id) return@SlackMessagePostedListener

        try {
            val messageContent = event.messageContent.trim()
            val splitCmd = messageContent.split("\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?".toRegex()).filterNot(String::isBlank)

            if (splitCmd.isNotEmpty() && isAddressedToBot(session.sessionPersona(), splitCmd[0])) {
                // indicate busy...
                session.addReactionToMessage(event.channel, event.timeStamp, "hourglass_flowing_sand")

                parseMessage(splitCmd)?.let { parsed ->
                    logger.info("Handling command '{}' from {}", messageContent, event.sender.userName)
                    parsed.groupStartMessage?.let { session.sendMessage(event.channel, it) }
                    parsed.actions.forEach { action -> handleAction(session, event, action, parsed) }

                } ?: run {
                    logger.warn("Ignored command '{}' from {}", messageContent, event.sender.userName)
                    session.addReactionToMessage(event.channel, event.timeStamp, "question")
                    printUsage(event, session)
                }
            }

        } catch(e: Exception) {
            logger.error("Error parsing message event: {}", event, e)
            session.addReactionToMessage(event.channel, event.timeStamp, "x")
            printUsage(event, session)
            return@SlackMessagePostedListener
        }
    })

    /**
     * Determine the Action to perform based on the provided command.
     */
    private fun parseMessage(splitCmd: List<String>): ActionWrapper? {
        val joinedMessage = splitCmd.joinToString()

        try {
            val context = templateService.fetchCandidates()

            // skip element 0, which contains the bot's username
            splitCmd.subList(1, splitCmd.size).forEach {
                token ->
                templateService.process(context, token)
            }

            // remove unsatisfied candidates
            when (context.candidates.filter { candidate -> candidate.tokens.isEmpty() }.size) {
                1 -> {
                    val candidate = context.candidates[0]
                    return ActionWrapper(candidate.buildActions(),
                            if (candidate.actionMessageMode == ActionMessageMode.GROUP) candidate.buildStartMessage() else null,
                            if (candidate.actionMessageMode == ActionMessageMode.GROUP) candidate.buildCompleteMessage() else null)
                }
                else -> throw IllegalStateException("Could not find a unique matching action for command: ${joinedMessage}")
            }

        } catch(e: IllegalStateException) {
            logger.warn("Unable to parse message: {} - {}", joinedMessage, e.message)
            return null
        }
    }

    /**
     * Post a message with usage information.
     */
    private fun printUsage(event: SlackMessagePosted, session: SlackSession) {
        val msg = StringBuilder()

        if (configService.actions().isEmpty()) {
            msg.append("Oops :broken_heart: you don't have any actions configured - add some to _${Settings.actionConfigFile}_")
        } else {
            msg.append("Sorry, I didn't understand :confused: Try typing _@${sessionService.botUsername} help_ for examples.")
        }

        session.sendMessage(event.channel, msg.toString())
    }

    /**
     * Check if the action is permitted, and if so, carry it out.
     */
    private fun handleAction(session: SlackSession, event: SlackMessagePosted, action: Action,
                             actionWrapper: ActionWrapper) {

        logger.info("Handling action: {}", action)

        authorisationService.checkPermission(action, { permitted ->
            if (permitted) {
                // respond with acknowledgement
                action.startMessage?.let { session.sendMessage(event.channel, it) }

                when (action) {
                    is CustomAction -> performCustomAction(session, event, action, actionWrapper)
                    else -> postSuccessfulReaction(session, event, true, actionWrapper)
                }

            } else {
                session.addReactionToMessage(event.channel, event.timeStamp, "no_entry")
                session.sendMessage(event.channel,
                        "Sorry, <@${event.sender.id}>, you're not allowed to perform ${action.shortDescription}.")
            }
        }, event.sender.userName)
    }

    /**
     * Perform the custom action and add a reaction with the outcome.
     */
    private fun performCustomAction(session: SlackSession, event: SlackMessagePosted, action: CustomAction,
                                    actionWrapper: ActionWrapper) {

        val trigger = TriggerContext(event.channel.id, event.sender.id, event.sender.userName, event.timestamp)

        // schedule action execution
        val request = PerformActionRequest.Builder.build(trigger, action.actionType, action.actionConfig, action.args)
        val future = actionPerformService.perform(request)

        future.whenComplete { (message, finalResult), throwable ->
            if (future.isCompletedExceptionally) {
                logger.error("Error performing custom action ${action}", throwable)

                session.addReactionToMessage(event.channel, event.timeStamp, "x")
                session.sendMessage(event.channel,
                        "Hmm, something went wrong :face_with_head_bandage:\r\n```${throwable.message}```")

            } else {
                postSuccessfulReaction(session, event, finalResult, actionWrapper)
                message?.let { session.sendMessage(event.channel, it) }
            }
        }
    }

    private fun postSuccessfulReaction(session: SlackSession, event: SlackMessagePosted, finalResult: Boolean,
                                       actionWrapper: ActionWrapper) {

        session.addReactionToMessage(event.channel, event.timeStamp, if (finalResult) "white_check_mark" else "ok")

        if (++actionWrapper.successful == actionWrapper.actions.size)
            actionWrapper.groupCompleteMessage?.let { session.sendMessage(event.channel, it) }
    }
}

/**
 * Determine if the message is addressed to the bot.
 */
fun isAddressedToBot(botPersona: SlackPersona, firstToken: String) =
        firstToken == "<@${botPersona.id}>" || firstToken == "<@${botPersona.id}>:"
