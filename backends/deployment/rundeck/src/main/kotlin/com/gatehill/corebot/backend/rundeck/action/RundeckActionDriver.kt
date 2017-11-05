package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.backend.jobs.action.JobBaseActionDriver
import com.gatehill.corebot.backend.jobs.operation.factory.JobOperationType
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.ActionDriver
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface RundeckActionDriver : ActionDriver

class RundeckActionDriverImpl @Inject constructor(triggerJobService: RundeckJobTriggerService,
                                                  lockService: LockService,
                                                  private val executionStatusService: ExecutionStatusService) : JobBaseActionDriver(triggerJobService, lockService), RundeckActionDriver {

    override fun handleAction(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                              operationType: OperationType, action: ActionConfig, args: Map<String, String>): Boolean {

        try {
            when (operationType) {
                JobOperationType.ENABLE -> executionStatusService.enableExecutions(future, action, false)
                JobOperationType.DISABLE -> executionStatusService.enableExecutions(future, action, true)
                else -> return super.handleAction(trigger, future, operationType, action, args)
            }
            return true

        } catch (e: Exception) {
            future.completeExceptionally(e)
            return false
        }
    }
}
