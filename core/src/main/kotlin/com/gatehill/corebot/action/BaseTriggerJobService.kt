package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.ActionStatus
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggeredAction
import com.gatehill.corebot.chat.ChatLines
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.config.model.ActionConfig
import org.apache.logging.log4j.LogManager
import java.util.*
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
    protected val pollCheckInterval = 2000L

    /**
     * Check lock status then optionally trigger execution.
     */
    override fun trigger(channelId: String, triggerMessageTimestamp: String,
                         future: CompletableFuture<PerformActionResult>,
                         action: ActionConfig, triggerMessageSenderName: String, args: Map<String, String>) {

        // user specified options override static values
        val allArgs = mutableMapOf<String, String>()
        allArgs.putAll(action.options.map { Pair(it.key, it.value.value) })
        allArgs.putAll(args)

        // trigger unless locked
        lockService.checkActionLock(action) { actionLock ->
            actionLock?.let {
                future.completeExceptionally(IllegalStateException(
                        "The '${action.name}' action is locked by <@${actionLock.owner}>"))

            } ?: run {
                lockService.checkOptionLock(action, allArgs) { optionLock ->
                    optionLock?.let {
                        future.completeExceptionally(IllegalStateException(
                                "${optionLock.optionName} ${optionLock.optionValue} is locked by <@${optionLock.owner}>"))

                    } ?: run {
                        logger.info("Triggering action: {} with job ID: {} and args: {}", action.name, action.jobId, allArgs)
                        triggerExecution(channelId, triggerMessageTimestamp, future, action,
                                triggerMessageSenderName,allArgs)
                    }
                }
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

        fetchExecutionOutput(channelId, triggerMessageTimestamp, action, executionId)
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
            pollExecutionInfo(channelId, triggerMessageTimestamp, action, executionDetails.id)

        } else {
            // already finished
            reactToFinalStatus(channelId, triggerMessageTimestamp, action, executionDetails.id, executionDetails.status)
        }
    }

    private fun pollExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                  executionId: Int, startTime: Long = System.currentTimeMillis()) {

        val blockDescription = "polling for *${action.name}* #${executionId} execution status"
        doUnlessTimedOut(channelId, startTime, triggerMessageTimestamp, blockDescription) {
            fetchExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, startTime)
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

    protected fun doUnlessTimedOut(channelId: String, startTime: Long, triggerMessageTimestamp: String,
                                   blockDescription: String, block: () -> Unit) {

        if (System.currentTimeMillis() - startTime < Settings.deployment.executionTimeout) {
            // enough time left to retry
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    block()
                }
            }, pollCheckInterval)

        } else {
            // timed out
            logger.error("Timed out '${blockDescription}' after ${Settings.deployment.executionTimeout}ms")

            sessionService.addReaction(channelId, triggerMessageTimestamp, "x")

            val timeoutSecs = TimeUnit.MILLISECONDS.toSeconds(Settings.deployment.executionTimeout.toLong())
            sessionService.sendMessage(channelId,
                    "Gave up ${blockDescription} after ${timeoutSecs} seconds.")
        }
    }

    protected fun processExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                       executionId: Int, startTime: Long, actionStatus: ActionStatus) {

        logger.info("Execution {} status: {}", executionId, actionStatus)

        if (actionStatus == ActionStatus.RUNNING) {
            pollExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, startTime)
        } else {
            reactToFinalStatus(channelId, triggerMessageTimestamp, action, executionId, actionStatus)
        }
    }

    protected fun sendOutput(channelId: String, action: ActionConfig, executionId: Int, output: String) {

        sessionService.sendMessage(channelId, "${action.name} #${executionId} finished with output: ${output}")
    }

    protected fun handleOutputFailure(action: ActionConfig, channelId: String, executionId: Int, cause: Throwable,
                                      triggerMessageTimestamp: String) {

        logger.error("Failed to check execution {} output", executionId, cause)
        notifyFailure(action, channelId, cause.message, triggerMessageTimestamp)
    }

    protected fun handleOutputError(action: ActionConfig, channelId: String, executionId: Int,
                                    triggerMessageTimestamp: String, errorMessage: String?) {

        logger.error("Error checking for execution {} output: {}", executionId, errorMessage)
        notifyFailure(action, channelId, errorMessage, triggerMessageTimestamp)
    }

    protected abstract fun triggerExecution(channelId: String, triggerMessageTimestamp: String,
                                            future: CompletableFuture<PerformActionResult>, action:
                                            ActionConfig, triggerMessageSenderName: String, args: Map<String, String>)

    protected abstract fun fetchExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                              executionId: Int, startTime: Long)

    protected abstract fun fetchExecutionOutput(channelId: String, triggerMessageTimestamp: String,
                                                action: ActionConfig, executionId: Int)
}
