package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.action.ActionService
import com.gatehill.rundeckbot.config.ActionConfig
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.Settings
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener
import org.apache.logging.log4j.LogManager

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ChatService {
    /**
     * Represents an action to perform.
     */
    data class Action(val actionType: ActionType,
                      val action: ActionConfig,
                      val args: Map<String, String>,
                      val actionMessage: String)

    private val logger = LogManager.getLogger(ChatService::class.java)!!
    private val settings = Settings()
    private val deploymentService = ActionService()
    private val templateService = TemplateService()
    private val configService = ConfigService()

    fun listenForEvents() {
        val session = SlackSessionFactory.createWebSocketSlackSession(settings.chat.authToken)
        session.connect()
        session.addMessagePostedListener(SlackMessagePostedListener { event, theSession ->
            // filter out messages from other channels
            val theChannel = theSession.findChannelByName(settings.chat.channelName)
            if (theChannel.id != event.channel.id) {
                return@SlackMessagePostedListener
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
                    val actions = parseMessage(splitCmd)
                    if (actions.size > 0) {
                        logger.info("Handling command '{}' from {}", messageContent, event.sender.userName)
                        actions.forEach { action -> handleAction(theSession, event, action) }

                    } else {
                        logger.warn("Skipped handling command '{}' from {}", messageContent, event.sender.userName)
                        printUsage(event, session)
                    }
                }

            } catch(e: Exception) {
                printUsage(event, session)
                logger.error("Error parsing message event: {}", event, e)
                return@SlackMessagePostedListener
            }
        })
    }

    private fun handleAction(session: SlackSession, event: SlackMessagePosted, action: Action) {
        logger.info("Handling action: {}", action)

        // respond with acknowledgement
        session.sendMessage(event.channel, action.actionMessage)

        // schedule action execution
        val future = deploymentService.perform(action.actionType, event.sender.userName, action.action, action.args)

        future.whenComplete { resultMessage, throwable ->
            if (future.isCompletedExceptionally) {
                session.sendMessage(event.channel,
                        "Hmm, something went wrong :face_with_head_bandage:\r\n```${throwable.message}```")
            } else {
                session.sendMessage(event.channel, resultMessage)
            }
        }
    }

    private fun printUsage(event: SlackMessagePosted, session: SlackSession) {
        val msg = StringBuilder()

        if (configService.loadActions().isEmpty()) {
            msg.append("Oops :broken_heart: you don't have any actions configured - add some to _${settings.configFile}_")

        } else {
            msg.append("Sorry, I didn't understand :slightly_frowning_face: Try one of these:")

            templateService.fetchCandidates().candidates.forEach { candidate ->
                val template = candidate.tokens.joinToString(" ")
                msg.append("\r\n_@${session.sessionPersona().userName} ${template}_")
            }
        }

        session.sendMessage(event.channel, msg.toString())
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
                        val actionTemplates = getActionAttribute(candidate.actions, { action -> action.template!! })
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
}
