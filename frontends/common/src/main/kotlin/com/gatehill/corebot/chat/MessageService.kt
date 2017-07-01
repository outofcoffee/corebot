package com.gatehill.corebot.chat

import com.gatehill.corebot.action.ActionPerformService
import com.gatehill.corebot.action.factory.ActionMessageMode
import com.gatehill.corebot.action.model.Action
import com.gatehill.corebot.action.model.ActionWrapper
import com.gatehill.corebot.action.model.CustomAction
import com.gatehill.corebot.action.model.PerformActionRequest
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.security.AuthorisationService
import com.gatehill.corebot.util.onException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Handles conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class MessageService @Inject constructor(private val sessionService: SessionService,
                                         private val templateService: TemplateService,
                                         private val configService: ConfigService,
                                         private val authorisationService: AuthorisationService,
                                         private val actionPerformService: ActionPerformService) {

    private val logger: Logger = LogManager.getLogger(MessageService::class.java)

    /**
     * Handle a message.
     */
    fun handleMessage(trigger: TriggerContext, commandOnly: String) {
        // indicate busy...
        sessionService.addReaction(trigger, "hourglass_flowing_sand")

        parseMessage(trigger, commandOnly)?.let { parsed ->
            logger.info("Handling command '$commandOnly' from ${trigger.username}")
            parsed.groupStartMessage?.let { sessionService.sendMessage(trigger, it) }
            parsed.actions.forEach { action -> handleAction(trigger, action, parsed) }

        } ?: run {
            logger.warn("Ignored command '$commandOnly' from ${trigger.username}")
            handleUnknownCommand(trigger)
        }
    }

    /**
     * Respond indicating the command was unknown.
     */
    fun handleUnknownCommand(trigger: TriggerContext) {
        sessionService.addReaction(trigger, "question")
        printUsage(trigger)
    }

    /**
     * Determine the Action to perform based on the provided command.
     */
    private fun parseMessage(trigger: TriggerContext, commandOnly: String): ActionWrapper? {
        try {
            templateService.findSatisfiedTemplates(commandOnly).let { satisfied ->
                if (satisfied.size == 1) {
                    return with(satisfied.first()) {
                        ActionWrapper(buildActions(trigger),
                                if (actionMessageMode == ActionMessageMode.GROUP) buildStartMessage(trigger) else null,
                                if (actionMessageMode == ActionMessageMode.GROUP) buildCompleteMessage() else null)
                    }
                } else {
                    throw IllegalStateException("Could not find a unique matching action for command: $commandOnly")
                }
            }

        } catch (e: IllegalStateException) {
            logger.warn("Unable to parse message: $commandOnly - ${e.message}")
            return null
        }
    }

    /**
     * Post a message with usage information.
     */
    fun printUsage(trigger: TriggerContext) {
        val msg = StringBuilder()

        if (configService.actions().isEmpty()) {
            msg.append("Oops :broken_heart: you don't have any actions configured - add some to _${Settings.actionConfigFile}_")
        } else {
            msg.append("Sorry, I didn't understand :confused: Try typing _@${sessionService.botUsername} help_ for examples.")
        }

        sessionService.sendMessage(trigger, msg.toString())
    }

    /**
     * Check if the action is permitted, and if so, carry it out.
     */
    private fun handleAction(trigger: TriggerContext, action: Action, actionWrapper: ActionWrapper) {
        logger.info("Handling action: $action")

        authorisationService.checkPermission(action, { permitted ->
            if (permitted) {
                // respond with acknowledgement
                action.startMessage?.let { sessionService.sendMessage(trigger, it) }

                when (action) {
                    is CustomAction -> performCustomAction(trigger, action, actionWrapper)
                    else -> postSuccessfulReaction(trigger, true, actionWrapper)
                }

            } else {
                sessionService.addReaction(trigger, "no_entry")
                sessionService.sendMessage(trigger,
                        "Sorry, <@${trigger.userId}>, you're not allowed to perform ${action.shortDescription}.")
            }
        }, trigger.username)
    }

    /**
     * Perform the custom action and add a reaction with the outcome.
     */
    private fun performCustomAction(trigger: TriggerContext, action: CustomAction, actionWrapper: ActionWrapper) {
        // schedule action execution
        val request = PerformActionRequest.Builder.build(trigger, action.actionType, action.actionConfig, action.args)

        actionPerformService.perform(request).thenAccept { (message, finalResult) ->
            postSuccessfulReaction(trigger, finalResult, actionWrapper)
            message?.let { sessionService.sendMessage(trigger, it) }

        }.onException { ex ->
            logger.error("Error performing custom action $action", ex)

            sessionService.addReaction(trigger, "x")
            sessionService.sendMessage(trigger,
                    "Hmm, something went wrong :face_with_head_bandage:\r\n```${ex.message}```")
        }
    }

    private fun postSuccessfulReaction(trigger: TriggerContext, finalResult: Boolean, actionWrapper: ActionWrapper) {
        sessionService.addReaction(trigger, if (finalResult) "white_check_mark" else "ok")

        if (++actionWrapper.successful == actionWrapper.actions.size)
            actionWrapper.groupCompleteMessage?.let { sessionService.sendMessage(trigger, it) }
    }
}
