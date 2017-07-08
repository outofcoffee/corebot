package com.gatehill.corebot.driver.jobs.service

import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
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
    fun trigger(trigger: TriggerContext,
                future: CompletableFuture<PerformActionResult>,
                action: ActionConfig, args: Map<String, String>)
}
