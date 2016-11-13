package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.ActionStatus
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggeredAction
import com.gatehill.corebot.chat.ChatLines
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.config.model.ActionConfig
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Triggers job executions, obtains status updates and notifies the user of the outcome.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class BaseTriggerJobService(private val lockService: LockService,
                                     private val sessionService: SessionService) : TriggerJobService {

    private val logger = LogManager.getLogger(BaseTriggerJobService::class.java)!!
    protected val statusCheckInterval = 2000L

    /**
     * Check lock status then optionally trigger execution.
     */
    override fun trigger(channelId: String, triggerMessageTimestamp: String,
                         future: CompletableFuture<PerformActionResult>,
                         action: ActionConfig, args: Map<String, String>) {

        lockService.checkLock(action) { lock ->
            if (null != lock) {
                future.completeExceptionally(IllegalStateException(
                        "The '${action.name}' action is locked by <@${lock.owner}>"))

            } else {
                val allArgs = args.plus(action.options.static ?: emptyMap())
                logger.info("Triggering action: {} with job ID: {} and args: {}", action.name, action.jobId, allArgs)
                triggerExecution(channelId, triggerMessageTimestamp, future, action, allArgs)
            }
        }
    }

    /**
     * Notify the user of the final status of the triggered action.
     */
    protected fun reactToFinalStatus(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
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

    /**
     * Check the status of the execution, either returning immediately if completed, falling back to polling.
     */
    protected fun checkStatus(action: ActionConfig, channelId: String, executionDetails: TriggeredAction,
                              future: CompletableFuture<PerformActionResult>, triggerMessageTimestamp: String) {

        val triggerEmoji = if (arrayOf(ActionStatus.RUNNING, ActionStatus.SUCCEEDED)
                .contains(executionDetails.status)) " :thumbsup:" else ""

        future.complete(PerformActionResult(
                "Job #${executionDetails.id} status: _${executionDetails.status.toSentenceCase()}_${triggerEmoji} (${executionDetails.url})", false))

        if (executionDetails.status == ActionStatus.RUNNING) {
            // poll for updates
            fetchExecutionInfo(channelId, triggerMessageTimestamp, action, executionDetails.id, System.currentTimeMillis())

        } else {
            // already finished
            reactToFinalStatus(channelId, triggerMessageTimestamp, action, executionDetails.id, executionDetails.status)
        }
    }

    protected fun handleStatusPollFailure(action: ActionConfig, channelId: String, executionId: Int, cause: Throwable,
                                          triggerMessageTimestamp: String) {

        logger.error("Failed to check execution {} status", executionId, cause)
        notifyFailure(action, channelId, cause.message, triggerMessageTimestamp)
    }

    protected fun handleStatusPollError(action: ActionConfig, channelId: String, executionId: Int,
                                        triggerMessageTimestamp: String, errorMessage: String?) {

        logger.error("Error checking for execution {} status: {}", executionId, errorMessage)
        notifyFailure(action, channelId, errorMessage, triggerMessageTimestamp)
    }

    private fun notifyFailure(action: ActionConfig, channelId: String, errorMessage: String?,
                              triggerMessageTimestamp: String) {

        sessionService.addReaction(channelId, triggerMessageTimestamp, "x")
        sessionService.sendMessage(channelId,
                "Error polling for *${action.name}* execution status:\r\n```$errorMessage```")
    }

    protected fun processExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                       executionId: Int, startTime: Long, actionStatus: ActionStatus) {

        logger.info("Execution {} status: {}", executionId, actionStatus)

        if (actionStatus == ActionStatus.RUNNING) {
            if (System.currentTimeMillis() - startTime < Settings.deployment.executionTimeout) {
                // enough time left to retry
                fetchExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, startTime)

            } else {
                // timed out
                logger.error("Timed out checking for execution {} status after {}ms",
                        executionId, Settings.deployment.executionTimeout)

                sessionService.addReaction(channelId, triggerMessageTimestamp, "x")

                val timeoutSecs = TimeUnit.MILLISECONDS.toSeconds(Settings.deployment.executionTimeout.toLong())
                sessionService.sendMessage(channelId,
                        "Gave up polling for *${action.name}* execution status after ${timeoutSecs} seconds.")
            }

        } else {
            reactToFinalStatus(channelId, triggerMessageTimestamp, action, executionId, actionStatus)
        }
    }

    protected abstract fun triggerExecution(channelId: String, triggerMessageTimestamp: String,
                                            future: CompletableFuture<PerformActionResult>, action:
                                            ActionConfig, args: Map<String, String>)

    protected abstract fun fetchExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                              executionId: Int, startTime: Long)
}
