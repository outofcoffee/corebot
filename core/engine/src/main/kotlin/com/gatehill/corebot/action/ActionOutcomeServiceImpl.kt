package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.ActionStatus
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.ChatLines
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.config.model.ActionConfig
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Handles the outcome of performing an action.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class ActionOutcomeServiceImpl @Inject constructor(private val sessionService: SessionService) : ActionOutcomeService {
    private val logger = LogManager.getLogger(ActionOutcomeServiceImpl::class.java)!!

    override fun notifyQueued(trigger: TriggerContext, action: ActionConfig) {
        sessionService.sendMessage(trigger.channelId, trigger.threadTimestamp,
                "Build for *${action.name}* is queued - ${ChatLines.pleaseWait().toLowerCase()}...")
    }

    override fun handleFinalStatus(trigger: TriggerContext, action: ActionConfig, executionId: Int,
                                   actionStatus: ActionStatus) {

        val reaction: String
        val emoji: String
        when (actionStatus) {
            ActionStatus.SUCCEEDED -> {
                reaction = ChatLines.goodNews()
                emoji = "white_check_mark"
            }
            else -> {
                reaction = ChatLines.badNews()
                emoji = "x"
            }
        }

        sessionService.addReaction(trigger.channelId, trigger.messageTimestamp, emoji)
        if(action.showJobOutcome) {
            sessionService.sendMessage(trigger.channelId, trigger.threadTimestamp,
                    "${reaction} *${action.name}* #${executionId} finished with status: _${actionStatus.toSentenceCase()}_.")
        }
    }

    override fun handlePollFailure(trigger: TriggerContext, action: ActionConfig, errorMessage: String?) {
        sessionService.addReaction(trigger.channelId, trigger.messageTimestamp, "x")
        sessionService.sendMessage(trigger.channelId, trigger.threadTimestamp,
                "Error polling for *${action.name}* execution status:\r\n```$errorMessage```")
    }

    override fun handleTimeout(trigger: TriggerContext, action: ActionConfig, blockDescription: String) {
        logger.error("Timed out '${blockDescription}' after ${Settings.deployment.executionTimeout}ms")

        sessionService.addReaction(trigger.channelId, trigger.messageTimestamp, "x")

        val timeoutSecs = TimeUnit.MILLISECONDS.toSeconds(Settings.deployment.executionTimeout.toLong())
        sessionService.sendMessage(trigger.channelId, trigger.threadTimestamp,
                "Gave up ${blockDescription} after ${timeoutSecs} seconds.")
    }

    override fun handleOutputFailure(trigger: TriggerContext, action: ActionConfig, errorMessage: String?) {
        sessionService.addReaction(trigger.channelId, trigger.messageTimestamp, "x")
        sessionService.sendMessage(trigger.channelId, trigger.threadTimestamp,
                "Error getting output for *${action.name}*:\r\n```$errorMessage```")
    }

    override fun handleFinalOutput(trigger: TriggerContext, action: ActionConfig, executionId: Int, output: String) {
        if (action.showJobOutput) {
            sessionService.sendMessage(trigger.channelId, trigger.threadTimestamp,
                    "${action.name} #${executionId} output: ${output}")
        }
    }

}
