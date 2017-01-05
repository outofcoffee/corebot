package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.ActionStatus
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Handles the outcome of performing an action and notifies the user appropriately.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ActionOutcomeService {
    /**
     * Notify the user that a job is queued.
     */
    fun notifyQueued(action: ActionConfig, channelId: String)

    /**
     * Notify the user of the final status of the triggered action.
     */
    fun reactToFinalStatus(channelId: String, triggerMessageTimestamp: String, action: ActionConfig, executionId: Int, actionStatus: ActionStatus)

    /**
     * Notify the user of a failure outcome.
     */
    fun handleFailure(action: ActionConfig, channelId: String, errorMessage: String?, triggerMessageTimestamp: String)

    /**
     * Notify the user of a timeout.
     */
    fun handleTimeout(blockDescription: String, channelId: String, triggerMessageTimestamp: String)
}
