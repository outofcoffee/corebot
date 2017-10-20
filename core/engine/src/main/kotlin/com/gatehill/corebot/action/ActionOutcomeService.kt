package com.gatehill.corebot.action

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.model.ActionStatus

/**
 * Handles the outcome of performing an action and notifies the user appropriately.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ActionOutcomeService {
    /**
     * Notify the user that a job is queued.
     */
    fun notifyQueued(trigger: TriggerContext, action: ActionConfig)

    /**
     * Notify the user of the final status of the triggered action.
     */
    fun handleFinalStatus(trigger: TriggerContext, action: ActionConfig, executionId: Int, actionStatus: ActionStatus)

    /**
     * Notify the user of a failure to poll for job status.
     */
    fun handlePollFailure(trigger: TriggerContext, action: ActionConfig, errorMessage: String?)

    /**
     * Notify the user of a timeout.
     */
    fun handleTimeout(trigger: TriggerContext, action: ActionConfig, blockDescription: String)

    /**
     * Notify the user of final output failure
     */
    fun handleOutputFailure(trigger: TriggerContext, action: ActionConfig, errorMessage: String?)

    /**
     * Notify the user the final output of the job
     */
    fun handleFinalOutput(trigger: TriggerContext, action: ActionConfig, executionId: Int, output: String)
}
