package com.gatehill.corebot.backend.jobs.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.BaseActionDriver
import com.gatehill.corebot.backend.jobs.action.factory.JobOperationType
import com.gatehill.corebot.backend.jobs.service.JobTriggerService
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class JobBaseActionDriver @Inject constructor(private val jobTriggerService: JobTriggerService,
                                                       lockService: LockService) : BaseActionDriver(lockService) {

    override fun handleAction(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                              operationType: OperationType, action: ActionConfig, args: Map<String, String>): Boolean {

        try {
            when (operationType) {
                JobOperationType.TRIGGER -> jobTriggerService.trigger(trigger, future, action, args)
                else -> return false
            }
            return true

        } catch (e: Exception) {
            future.completeExceptionally(e)
            return false
        }
    }
}
