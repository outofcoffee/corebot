package com.gatehill.corebot.action.driver

import com.gatehill.corebot.action.JobTriggerService
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.template.JobActionType
import com.gatehill.corebot.config.model.ActionConfig
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class JobBaseActionDriver @Inject constructor(private val jobTriggerService: JobTriggerService,
                                                       lockService: LockService) : BaseActionDriver(lockService) {

    override fun handleAction(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                              actionType: ActionType, action: ActionConfig, args: Map<String, String>): Boolean {

        try {
            when (actionType) {
                JobActionType.TRIGGER -> jobTriggerService.trigger(trigger, future, action, args)
                else -> return false
            }
            return true

        } catch(e: Exception) {
            future.completeExceptionally(e)
            return false
        }
    }
}
