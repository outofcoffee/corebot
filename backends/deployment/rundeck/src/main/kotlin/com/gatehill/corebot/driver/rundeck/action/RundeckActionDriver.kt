package com.gatehill.corebot.driver.rundeck.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.driver.ActionDriver
import com.gatehill.corebot.action.driver.JobBaseActionDriver
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.template.JobActionType
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.base.action.ApiClientBuilder
import com.gatehill.corebot.driver.rundeck.config.DriverSettings
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface RundeckActionDriver : ActionDriver, ApiClientBuilder<RundeckApi>

class RundeckActionDriverImpl @Inject constructor(triggerJobService: RundeckJobTriggerService,
                                                  lockService: LockService,
                                                  private val executionStatusService: ExecutionStatusService) : JobBaseActionDriver(triggerJobService, lockService), RundeckActionDriver {
    override val baseUrl: String
        get() = DriverSettings.deployment.baseUrl

    override fun buildApiClient(headers: Map<String, String>): RundeckApi {
        val allHeaders: MutableMap<String, String> = HashMap(headers)
        allHeaders.put("X-Rundeck-Auth-Token", DriverSettings.deployment.apiToken)
        return buildApiClient(RundeckApi::class.java, allHeaders)
    }

    override fun handleAction(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                              actionType: ActionType, action: ActionConfig, args: Map<String, String>): Boolean {

        try {
            when (actionType) {
                JobActionType.ENABLE -> executionStatusService.enableExecutions(future, action, false)
                JobActionType.DISABLE -> executionStatusService.enableExecutions(future, action, true)
                else -> return super.handleAction(trigger, future, actionType, action, args)
            }
            return true

        } catch(e: Exception) {
            future.completeExceptionally(e)
            return false
        }
    }
}
