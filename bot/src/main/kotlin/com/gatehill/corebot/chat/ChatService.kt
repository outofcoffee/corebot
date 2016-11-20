package com.gatehill.corebot.chat

import com.gatehill.corebot.action.ActionDriverFactory
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
import javax.inject.Inject

/**
 * Handles conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ChatService @Inject constructor(private val sessionService: SlackSessionService,
                                      private val templateService: TemplateService,
                                      private val configService: ConfigService,
                                      private val authorisationService: AuthorisationService,
                                      private val actionDriverFactory: ActionDriverFactory) {

    private val logger = LogManager.getLogger(ChatService::class.java)!!

    fun listenForEvents() {
        val session = sessionService.session

        session.addMessagePostedListener(SlackMessagePostedListener { event, theSession ->
            // filter out messages from other channels
            if (!Settings.chat.channelNames.map { channelName -> theSession.findChannelByName(channelName).id }
                    .contains(event.channel.id)) return@SlackMessagePostedListener

            // ignore own messages
            if (theSession.sessionPersona().id == event.sender.id) return@SlackMessagePostedListener

            try {
                val messageContent = event.messageContent.trim()
                val splitCmd = messageContent.split("\\s".toRegex()).filterNot(String::isBlank)

                if (splitCmd.isNotEmpty() && isAddressedToBot(session.sessionPersona(), splitCmd[0])) {
                    // indicate busy...
                    session.addReactionToMessage(event.channel, event.timeStamp, "hourglass_flowing_sand")

                    val parsed = parseMessage(splitCmd)
                    parsed?.let {
                        logger.info("Handling command '{}' from {}", messageContent, event.sender.userName)
                        parsed.groupStartMessage?.let { session.sendMessage(event.channel, it) }
                        parsed.actions.forEach { action -> handleAction(theSession, event, action, parsed) }

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
    }

    private fun isAddressedToBot(botPersona: SlackPersona, firstToken: String) =
            firstToken == "<@${botPersona.id}>" || firstToken == "<@${botPersona.id}>:"

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

            if (1 == context.candidates.size) {
                val candidate = context.candidates[0]

                if (candidate.tokens.isNotEmpty()) {
                    throw IllegalStateException("Too few tokens for actions: ${candidate.actionTemplates}")
                }

                return ActionWrapper(candidate.buildActions(),
                        if (candidate.actionMessageMode == ActionMessageMode.GROUP) candidate.buildStartMessage() else null,
                        if (candidate.actionMessageMode == ActionMessageMode.GROUP) candidate.buildCompleteMessage() else null)

            } else {
                throw IllegalStateException("Could not find a unique matching action for command: ${joinedMessage}")
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
            msg.append("Oops :broken_heart: you don't have any actions configured - add some to _${Settings.configFile}_")
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

        // schedule action execution
        val actionDriver = actionDriverFactory.driverFor(action.driver)
        val future = actionDriver.perform(event.channel.id, event.sender.id, event.timestamp,
                action.actionType, action.actionConfig, action.args)

        future.whenComplete { result, throwable ->
            if (future.isCompletedExceptionally) {
                logger.error("Error performing custom action ${action}", throwable)

                session.addReactionToMessage(event.channel, event.timeStamp, "x")
                session.sendMessage(event.channel,
                        "Hmm, something went wrong :face_with_head_bandage:\r\n```${throwable.message}```")

            } else {
                postSuccessfulReaction(session, event, result.finalResult, actionWrapper)
                result.message?.let { session.sendMessage(event.channel, it) }
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
