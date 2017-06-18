package com.gatehill.corebot.chat

import com.gatehill.corebot.action.ActionPerformService
import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.model.ActionOperation
import com.gatehill.corebot.operation.model.Operation
import com.gatehill.corebot.operation.model.OperationContext
import com.gatehill.corebot.operation.model.PerformActionRequest
import com.gatehill.corebot.operation.model.PlainOperation
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.template.FactoryService
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
                                         private val factoryService: FactoryService,
                                         private val configService: ConfigService,
                                         private val authorisationService: AuthorisationService,
                                         private val actionPerformService: ActionPerformService,
                                         private val chatGenerator: ChatGenerator) {

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
            parsed.operations.forEach { action -> performOperation(trigger, action, parsed) }

        } ?: run {
            logger.warn("Ignored command '$commandOnly' from ${trigger.username}")
            handleUnknownCommand(trigger)
        }
    }

    /**
     * Determine the Operation to perform based on the provided command.
     */
    private fun parseMessage(trigger: TriggerContext, commandOnly: String): OperationContext? = try {
        factoryService.findSatisfiedFactories(commandOnly).let { satisfied ->
            if (satisfied.size == 1) {
                return with(satisfied.first()) {
                    OperationContext(buildOperations(trigger),
                            if (operationMessageMode == OperationMessageMode.GROUP) buildStartMessage(trigger) else null,
                            if (operationMessageMode == OperationMessageMode.GROUP) buildCompleteMessage() else null)
                }
            } else {
                throw IllegalStateException("Could not find a unique matching operation for command: $commandOnly - ${satisfied.size} factories found: $satisfied")
            }
        }

    } catch (e: IllegalStateException) {
        logger.warn("Unable to parse message: $commandOnly - ${e.message}")
        null
    }

    /**
     * Respond indicating the command was unknown.
     */
    fun handleUnknownCommand(trigger: TriggerContext) {
        sessionService.addReaction(trigger, "question")
        printUsage(trigger)
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
     * Check if the operation is permitted, and if so, carry it out.
     */
    private fun performOperation(trigger: TriggerContext, operation: Operation, operationContext: OperationContext) {
        logger.info("Performing operation: $operation")

        authorisationService.checkPermission(operation, { permitted ->
            if (permitted) {
                // respond with acknowledgement
                operation.startMessage?.let { sessionService.sendMessage(trigger, it) }

                try {
                    operation.operationFactory.beforePerform(trigger)

                    when (operation) {
                        is PlainOperation -> postSuccessfulReaction(trigger, true, operationContext)
                        is ActionOperation -> performActionOperation(trigger, operation, operationContext)
                        else -> {
                            sessionService.addReaction(trigger, "x")
                            sessionService.sendMessage(trigger, "Unsupported operation type: ${operation::class.java.simpleName}")
                        }
                    }

                } catch (e: Exception) {
                    handleOperationException(trigger, operation, e)
                }

            } else {
                sessionService.addReaction(trigger, "no_entry")
                sessionService.sendMessage(trigger,
                        "Sorry, <@${trigger.userId}>, you're not allowed to perform ${operation.shortDescription}.")
            }
        }, trigger.username)
    }

    /**
     * Perform the action operation and add a reaction with the outcome.
     */
    private fun performActionOperation(trigger: TriggerContext, operation: ActionOperation, operationContext: OperationContext) {
        // schedule action execution
        val request = PerformActionRequest.Builder.build(trigger, operation.operationType, operation.actionConfig, operation.args)

        actionPerformService.perform(request).thenAccept { (message, finalResult) ->
            postSuccessfulReaction(trigger, finalResult, operationContext)
            message?.let { sessionService.sendMessage(trigger, it) }

        }.onException { ex ->
            handleOperationException(trigger, operation, ex)
        }
    }

    private fun postSuccessfulReaction(trigger: TriggerContext, finalResult: Boolean, operationContext: OperationContext) {
        sessionService.addReaction(trigger, if (finalResult) "white_check_mark" else "ok")

        if (++operationContext.successful == operationContext.operations.size)
            operationContext.groupCompleteMessage?.let { sessionService.sendMessage(trigger, it) }
    }

    private fun handleOperationException(trigger: TriggerContext, operation: Operation, ex: Throwable) {
        logger.error("Error performing action operation $operation", ex)

        sessionService.addReaction(trigger, "x")
        sessionService.sendMessage(trigger,
                "${chatGenerator.badNews()} something went wrong\r\n```${ex.message}```")
    }
}
