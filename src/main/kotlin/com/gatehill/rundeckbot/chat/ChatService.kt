package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.action.ActionService
import com.gatehill.rundeckbot.config.ActionConfig
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.Settings
import com.gatehill.rundeckbot.security.AuthorisationService
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener
import org.apache.logging.log4j.LogManager

/**
 * Handles conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ChatService {
    /**
     * Represents an action to perform.
     */
    data class Action(val actionType: ActionType,
                      val action: ActionConfig,
                      val args: Map<String, String>,
                      val actionMessage: String)

    private val logger = LogManager.getLogger(ChatService::class.java)!!
    private val deploymentService by lazy { ActionService }
    private val templateService by lazy { TemplateService }
    private val configService by lazy { ConfigService }
    private val authorisationService by lazy { AuthorisationService }

    fun listenForEvents() {
        val session = SlackSessionFactory.createWebSocketSlackSession(Settings.chat.authToken)
        session.connect()
        session.addMessagePostedListener(SlackMessagePostedListener { event, theSession ->
            // filter out messages from other channels
            for (channelName in Settings.chat.channelNames) {
                val theChannel = theSession.findChannelByName(channelName)
                if (theChannel.id != event.channel.id) {
                    return@SlackMessagePostedListener
                }
            }

            // ignore own messages
            if (theSession.sessionPersona().id == event.sender.id) {
                return@SlackMessagePostedListener
            }

            try {
                val messageContent = event.messageContent
                val splitCmd = messageContent.split("\\s".toRegex())

                // is it addressed to the bot?
                if (splitCmd.size > 0 && splitCmd[0] == "<@${session.sessionPersona().id}>") {
                    // indicate busy...
                    session.addReactionToMessage(event.channel, event.timeStamp, "hourglass_flowing_sand")

                    val actions = parseMessage(splitCmd)
                    if (actions.size > 0) {
                        logger.info("Handling command '{}' from {}", messageContent, event.sender.userName)
                        actions.forEach { action -> handleAction(theSession, event, action) }

                    } else {
                        logger.warn("Skipped handling command '{}' from {}", messageContent, event.sender.userName)
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

            when (context.candidates.size) {
                1 -> {
                    val candidate = context.candidates[0]

                    if (candidate.tokens.size > 0) {
                        val actionTemplates = readActionAttribute(candidate.actions, ActionConfig::template)
                        throw IllegalStateException("Too few tokens for actions: ${actionTemplates}")
                    }

                    return candidate.actions.map { actionConfig ->
                        Action(candidate.actionType, actionConfig, candidate.placeholderValues, candidate.buildMessage(actionConfig))
                    }
                }
                else -> throw IllegalStateException("Could not find a unique matching action for command: $joinedMessage")
            }

        } catch(e: IllegalStateException) {
            logger.warn("Unable to parse message: {} - {}", joinedMessage, e.message)
            return emptyList()
        }
    }

    private fun printUsage(event: SlackMessagePosted, session: SlackSession) {
        val msg = StringBuilder()

        if (configService.loadActions().isEmpty()) {
            msg.append("Oops :broken_heart: you don't have any actions configured - add some to _${Settings.configFile}_")

        } else {
            msg.append("Sorry, I didn't understand :slightly_frowning_face: Try one of these:")

            templateService.fetchCandidates().candidates.forEach { candidate ->
                val template = candidate.tokens.joinToString(" ")
                msg.append("\r\n_@${session.sessionPersona().userName} ${template}_")
            }
        }

        session.sendMessage(event.channel, msg.toString())
    }

    private fun handleAction(session: SlackSession, event: SlackMessagePosted, action: Action) {
        logger.info("Handling action: {}", action)

        authorisationService.checkPermission(action, { permitted ->
            if (!permitted) {
                session.addReactionToMessage(event.channel, event.timeStamp, "no_entry")
                session.sendMessage(event.channel, "Sorry, <@${event.sender.id}>, you're not allowed to perform" +
                        " _${action.actionType.description}_ on *${action.action.name}*.")

            } else {
                // respond with acknowledgement
                session.sendMessage(event.channel, action.actionMessage)

                // schedule action execution
                val future = deploymentService.perform(session, event, action.actionType, action.action, action.args)

                future.whenComplete { result, throwable ->
                    if (future.isCompletedExceptionally) {
                        session.addReactionToMessage(event.channel, event.timeStamp, "x")
                        session.sendMessage(event.channel,
                                "Hmm, something went wrong :face_with_head_bandage:\r\n```${throwable.message}```")

                    } else {
                        session.addReactionToMessage(event.channel, event.timeStamp,
                                if (result.finalResult) "white_check_mark" else "ok")

                        session.sendMessage(event.channel, result.message)
                    }
                }
            }
        }, event.sender.userName)
    }
}
