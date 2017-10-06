package com.gatehill.corebot.driver.jobs.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.BaseActionDriver
import com.gatehill.corebot.driver.jobs.action.factory.JobOperationType
import com.gatehill.corebot.driver.jobs.service.JobTriggerService
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
