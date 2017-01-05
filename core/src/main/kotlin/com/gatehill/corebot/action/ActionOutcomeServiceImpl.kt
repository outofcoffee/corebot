package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.ActionStatus
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

    override fun notifyQueued(action: ActionConfig, channelId: String) {
        sessionService.sendMessage(channelId, "Build for *${action.name}* is queued - ${ChatLines.pleaseWait().toLowerCase()}...")
    }

    override fun handleFinalStatus(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                   executionId: Int, actionStatus: ActionStatus) {

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

        sessionService.addReaction(channelId, triggerMessageTimestamp, emoji)
        sessionService.sendMessage(channelId,
                "${reaction} *${action.name}* #${executionId} finished with status: _${actionStatus.toSentenceCase()}_.")
    }

    override fun handlePollFailure(action: ActionConfig, channelId: String, errorMessage: String?,
                                   triggerMessageTimestamp: String) {

        sessionService.addReaction(channelId, triggerMessageTimestamp, "x")
        sessionService.sendMessage(channelId,
                "Error polling for *${action.name}* execution status:\r\n```$errorMessage```")
    }

    override fun handleTimeout(action: ActionConfig, blockDescription: String, channelId: String, triggerMessageTimestamp: String) {
        logger.error("Timed out '${blockDescription}' after ${Settings.deployment.executionTimeout}ms")

        sessionService.addReaction(channelId, triggerMessageTimestamp, "x")

        val timeoutSecs = TimeUnit.MILLISECONDS.toSeconds(Settings.deployment.executionTimeout.toLong())
        sessionService.sendMessage(channelId,
                "Gave up ${blockDescription} after ${timeoutSecs} seconds.")
    }
}
