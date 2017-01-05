package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.ActionStatus
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggeredAction
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.config.model.ActionConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Triggers job executions, obtains status updates and notifies the user of the outcome.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class BaseJobTriggerService(private val lockService: LockService,
                                     private val actionOutcomeService: ActionOutcomeService) : JobTriggerService {

    private val logger: Logger = LogManager.getLogger(BaseJobTriggerService::class.java)
    protected val pollCheckInterval = 2000L

    /**
     * Check lock status then optionally trigger execution.
     */
    override fun trigger(channelId: String, triggerMessageTimestamp: String,
                         future: CompletableFuture<PerformActionResult>,
                         action: ActionConfig, args: Map<String, String>) {

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
                        triggerExecution(channelId, triggerMessageTimestamp, future, action, allArgs)
                    }
                }
            }
        }
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
            actionOutcomeService.handleFinalStatus(channelId, triggerMessageTimestamp, action, executionDetails.id, executionDetails.status)
        }
    }

    private fun pollExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                  executionId: Int, startTime: Long = System.currentTimeMillis()) {

        val blockDescription = "polling for *${action.name}* #${executionId} execution status"
        doUnlessTimedOut(action, channelId, startTime, triggerMessageTimestamp, blockDescription) {
            fetchExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, startTime)
        }
    }

    protected fun handleStatusPollFailure(action: ActionConfig, channelId: String, executionId: Int, cause: Throwable,
                                          triggerMessageTimestamp: String) {

        logger.error("Failed to check execution {} status", executionId, cause)
        actionOutcomeService.handlePollFailure(action, channelId, cause.message, triggerMessageTimestamp)
    }

    protected fun handleStatusPollError(action: ActionConfig, channelId: String, executionId: Int,
                                        triggerMessageTimestamp: String, errorMessage: String?) {

        logger.error("Error checking for execution {} status: {}", executionId, errorMessage)
        actionOutcomeService.handlePollFailure(action, channelId, errorMessage, triggerMessageTimestamp)
    }

    protected fun doUnlessTimedOut(action: ActionConfig, channelId: String, startTime: Long, triggerMessageTimestamp: String,
                                   blockDescription: String, block: () -> Unit) {

        if (System.currentTimeMillis() - startTime < Settings.deployment.executionTimeout) {
            // enough time left to retry
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    block()
                }
            }, pollCheckInterval)

        } else {
            actionOutcomeService.handleTimeout(action, blockDescription, channelId, triggerMessageTimestamp)
        }
    }

    protected fun processExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                       executionId: Int, startTime: Long, actionStatus: ActionStatus) {

        logger.info("Execution {} status: {}", executionId, actionStatus)

        if (actionStatus == ActionStatus.RUNNING) {
            pollExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, startTime)
        } else {
            actionOutcomeService.handleFinalStatus(channelId, triggerMessageTimestamp, action, executionId, actionStatus)
        }
    }

    protected abstract fun triggerExecution(channelId: String, triggerMessageTimestamp: String,
                                            future: CompletableFuture<PerformActionResult>, action:
                                            ActionConfig, args: Map<String, String>)

    protected abstract fun fetchExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                              executionId: Int, startTime: Long)
}
