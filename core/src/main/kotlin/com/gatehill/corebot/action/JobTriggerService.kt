package com.gatehill.corebot.action

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.config.model.ActionConfig
import java.util.concurrent.CompletableFuture

/**
 * Triggers job executions and obtains status updates.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface JobTriggerService {
    /**
     * Trigger execution of a job, then poll for status updates.
     */
    fun trigger(channelId: String, triggerMessageTimestamp: String,
                future: CompletableFuture<PerformActionResult>,
                action: ActionConfig, args: Map<String, String>)
}
