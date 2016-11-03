package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.action.ActionService
import com.gatehill.rundeckbot.chat.model.Action
import com.gatehill.rundeckbot.chat.model.CustomAction
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.Settings
import com.gatehill.rundeckbot.config.model.ActionConfig
import com.gatehill.rundeckbot.config.model.readActionConfigAttribute
import com.gatehill.rundeckbot.security.AuthorisationService
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener
import org.apache.logging.log4j.LogManager

/**
 * Handles conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ChatService {
    private val logger = LogManager.getLogger(ChatService::class.java)!!
    private val sessionService by lazy { SessionService }
    private val deploymentService by lazy { ActionService }
    private val templateService by lazy { TemplateService }
    private val configService by lazy { ConfigService }
    private val authorisationService by lazy { AuthorisationService }

    fun listenForEvents() {
        val session = sessionService.session

        session.addMessagePostedListener(SlackMessagePostedListener { event, theSession ->
            // filter out messages from other channels
            for (channelName in Settings.chat.channelNames) {
                if (theSession.findChannelByName(channelName).id != event.channel.id) return@SlackMessagePostedListener
            }

            // ignore own messages
            if (theSession.sessionPersona().id == event.sender.id) return@SlackMessagePostedListener

            try {
                val messageContent = event.messageContent
                val splitCmd = messageContent.split("\\s".toRegex()).filterNot(String::isBlank)

                // is it addressed to the bot?
                if (splitCmd.size > 0 && splitCmd[0] == "<@${session.sessionPersona().id}>") {
                    // indicate busy...
                    session.addReactionToMessage(event.channel, event.timeStamp, "hourglass_flowing_sand")

                    val actions = parseMessage(splitCmd)
                    if (actions.size > 0) {
                        logger.info("Handling command '{}' from {}", messageContent, event.sender.userName)
                        actions.forEach { action -> handleAction(theSession, event, action) }

                    } else {
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

    /**
     * Determine the Action to perform based on the provided command.
     */
    private fun parseMessage(splitCmd: List<String>): List<Action> {
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

                if (candidate.tokens.size > 0) {
                    val actionTemplates = readActionConfigAttribute(candidate.actionConfigs, ActionConfig::template)
                    throw IllegalStateException("Too few tokens for actions: ${actionTemplates}")
                }

                return candidate.buildActions()

            } else {
                throw IllegalStateException("Could not find a unique matching action for command: ${joinedMessage}")
            }

        } catch(e: IllegalStateException) {
            logger.warn("Unable to parse message: {} - {}", joinedMessage, e.message)
            return emptyList()
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
            msg.append("Sorry, I didn't understand :confused: Try one of these:")
            msg.appendln(); msg.append(templateService.usage())
        }

        session.sendMessage(event.channel, msg.toString())
    }

    /**
     * Check if the action is permitted, and if so, carry it out.
     */
    private fun handleAction(session: SlackSession, event: SlackMessagePosted, action: Action) {
        logger.info("Handling action: {}", action)

        authorisationService.checkPermission(action, { permitted ->
            if (permitted) {
                // respond with acknowledgement
                session.sendMessage(event.channel, action.actionMessage)

                if (action is CustomAction) {
                    performCustomAction(session, event, action)
                } else {
                    // short circuit to success
                    postSuccessfulReaction(session, event, true)
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
    private fun performCustomAction(session: SlackSession, event: SlackMessagePosted, action: CustomAction) {
        // schedule action execution
        val future = deploymentService.perform(session, event, action.actionType, action.actionConfig, action.args)

        future.whenComplete { result, throwable ->
            if (future.isCompletedExceptionally) {
                session.addReactionToMessage(event.channel, event.timeStamp, "x")
                session.sendMessage(event.channel,
                        "Hmm, something went wrong :face_with_head_bandage:\r\n```${throwable.message}```")

            } else {
                postSuccessfulReaction(session, event, result.finalResult)
                session.sendMessage(event.channel, result.message)
            }
        }
    }

    private fun postSuccessfulReaction(session: SlackSession, event: SlackMessagePosted, finalResult: Boolean) {
        session.addReactionToMessage(event.channel, event.timeStamp, if (finalResult) "white_check_mark" else "ok")
    }
}
