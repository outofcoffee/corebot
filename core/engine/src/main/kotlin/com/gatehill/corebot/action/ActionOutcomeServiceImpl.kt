package com.gatehill.corebot.action

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.model.ActionStatus
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Handles the outcome of performing an action.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class ActionOutcomeServiceImpl @Inject constructor(private val sessionService: SessionService,
                                                        private val chatGenerator: ChatGenerator) : ActionOutcomeService {

    private val logger = LogManager.getLogger(ActionOutcomeServiceImpl::class.java)!!

    override fun notifyQueued(trigger: TriggerContext, action: ActionConfig) {
        sessionService.sendMessage(trigger,
                "Build for *${action.name}* is queued - ${chatGenerator.pleaseWait().toLowerCase()}...")
    }

    override fun handleFinalStatus(trigger: TriggerContext, action: ActionConfig, executionId: Int,
                                   actionStatus: ActionStatus) {

        val (reaction, emoji) = when (actionStatus) {
            ActionStatus.SUCCEEDED -> chatGenerator.goodNews() to "white_check_mark"
            else -> chatGenerator.badNews() to "x"
        }

        sessionService.addReaction(trigger, emoji)
        if (action.showJobOutcome) {
            sessionService.sendMessage(trigger,
                    "$reaction *${action.name}* #$executionId finished with status: _${actionStatus.toSentenceCase()}_.")
        }
    }

    override fun handlePollFailure(trigger: TriggerContext, action: ActionConfig, errorMessage: String?) {
        sessionService.addReaction(trigger, "x")
        sessionService.sendMessage(trigger,
                "Error polling for *${action.name}* execution status:\r\n```$errorMessage```")
    }

    override fun handleTimeout(trigger: TriggerContext, action: ActionConfig, blockDescription: String) {
        logger.error("Timed out '$blockDescription' after ${Settings.execution.executionTimeout}ms")
        sessionService.addReaction(trigger, "x")

        val timeoutSecs = TimeUnit.MILLISECONDS.toSeconds(Settings.execution.executionTimeout.toLong())
        sessionService.sendMessage(trigger, "Gave up $blockDescription after $timeoutSecs seconds.")
    }

    override fun handleOutputFailure(trigger: TriggerContext, action: ActionConfig, errorMessage: String?) {
        sessionService.addReaction(trigger, "x")
        sessionService.sendMessage(trigger, "Error getting output for *${action.name}*:\r\n```$errorMessage```")
    }

    override fun handleFinalOutput(trigger: TriggerContext, action: ActionConfig, executionId: Int, output: String) {
        if (action.showJobOutput) {
            sessionService.sendMessage(trigger, "${action.name} #$executionId output: $output")
        }
    }
}
