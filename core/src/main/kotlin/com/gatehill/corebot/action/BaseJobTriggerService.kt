package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.ActionStatus
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
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
    override fun trigger(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
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
                        triggerExecution(trigger, future, action, allArgs)
                    }
                }
            }
        }
    }

    /**
     * Check the status of the execution, either returning immediately if completed, falling back to polling.
     */
    protected fun checkStatus(trigger: TriggerContext, action: ActionConfig, executionDetails: TriggeredAction,
                              future: CompletableFuture<PerformActionResult>) {

        val triggerEmoji = if (arrayOf(ActionStatus.RUNNING, ActionStatus.SUCCEEDED)
                .contains(executionDetails.status)) " :thumbsup:" else ""

        future.complete(PerformActionResult(
                "Job #${executionDetails.id} status: _${executionDetails.status.toSentenceCase()}_${triggerEmoji} (${executionDetails.url})", false))

        if (executionDetails.status == ActionStatus.RUNNING) {
            // poll for updates
            pollExecutionInfo(trigger, action, executionDetails.id)

        } else {
            // already finished
            actionOutcomeService.handleFinalStatus(trigger, action, executionDetails.id, executionDetails.status)
            if (action.options.get("show-job-output")?.value == "true") {
                fetchExecutionOutput(trigger, action, executionDetails.id)
            }
        }
    }

    private fun pollExecutionInfo(trigger: TriggerContext, action: ActionConfig,
                                  executionId: Int, startTime: Long = System.currentTimeMillis()) {

        val blockDescription = "polling for *${action.name}* #${executionId} execution status"
        doUnlessTimedOut(trigger, action, startTime, blockDescription) {
            fetchExecutionInfo(trigger, action, executionId, startTime)
        }
    }

    protected fun handleStatusPollFailure(trigger: TriggerContext, action: ActionConfig, executionId: Int,
                                          cause: Throwable) {

        logger.error("Failed to check execution {} status", executionId, cause)
        actionOutcomeService.handlePollFailure(trigger, action, cause.message)
    }

    protected fun handleStatusPollError(trigger: TriggerContext, action: ActionConfig, executionId: Int,
                                        errorMessage: String?) {

        logger.error("Error checking for execution {} status: {}", executionId, errorMessage)
        actionOutcomeService.handlePollFailure(trigger, action, errorMessage)
    }

    protected fun doUnlessTimedOut(trigger: TriggerContext, action: ActionConfig, startTime: Long,
                                   blockDescription: String, block: () -> Unit) {

        if (System.currentTimeMillis() - startTime < Settings.deployment.executionTimeout) {
            // enough time left to retry
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    block()
                }
            }, pollCheckInterval)

        } else {
            actionOutcomeService.handleTimeout(trigger, action, blockDescription)
        }
    }

    protected fun processExecutionInfo(trigger: TriggerContext, action: ActionConfig,
                                       executionId: Int, startTime: Long, actionStatus: ActionStatus) {

        logger.info("Execution {} status: {}", executionId, actionStatus)

        if (actionStatus == ActionStatus.RUNNING) {
            pollExecutionInfo(trigger, action, executionId, startTime)
        } else {
            actionOutcomeService.handleFinalStatus(trigger, action, executionId, actionStatus)
            if (action.options.get("show-job-output")?.value == "true") {
                fetchExecutionOutput(trigger, action, executionId)
            }
        }
    }

    protected fun sendOutput(trigger: TriggerContext, action: ActionConfig, executionId: Int, output: String) {

        actionOutcomeService.handleFinalOutput(trigger, action, executionId, output)
    }

    protected fun handleOutputFailure(trigger: TriggerContext, action: ActionConfig, executionId: Int,
                                      cause: Throwable) {

        logger.error("Failed to check execution {} output", executionId, cause)
        actionOutcomeService.handleOutputFailure(trigger, action, cause.message)
    }

    protected fun handleOutputError(trigger: TriggerContext, action: ActionConfig, executionId: Int,
                                      errorMessage: String?) {

        logger.error("Failed to check execution {} output", executionId, errorMessage)
        actionOutcomeService.handleOutputFailure(trigger, action, errorMessage)
    }

    protected abstract fun triggerExecution(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                                            action: ActionConfig, args: Map<String, String>)

    protected abstract fun fetchExecutionInfo(trigger: TriggerContext, action: ActionConfig, executionId: Int,
                                              startTime: Long)

    protected abstract fun fetchExecutionOutput(trigger: TriggerContext, action: ActionConfig, executionId: Int)
}
