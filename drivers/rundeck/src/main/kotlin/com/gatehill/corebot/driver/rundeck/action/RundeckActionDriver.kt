package com.gatehill.corebot.driver.rundeck.action

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.corebot.action.ActionDriver
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.rundeck.config.DriverSettings
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface RundeckActionDriver : ActionDriver {
    fun buildRundeckApi(): RundeckApi
}

class RundeckActionDriverImpl @Inject constructor(private val triggerJobService: TriggerJobService,
                                                  private val executionStatusService: ExecutionStatusService,
                                                  private val lockService: LockService) : RundeckActionDriver {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Trigger the specified action with the given arguments.
     */
    override fun perform(channelId: String, triggerMessageSenderId: String, triggerMessageTimestamp: String,
                         actionType: ActionType, action: ActionConfig,
                         args: Map<String, String>): CompletableFuture<PerformActionResult> {

        val future = CompletableFuture<PerformActionResult>()
        try {
            when (actionType) {
                ActionType.TRIGGER -> triggerJobService.trigger(channelId, triggerMessageTimestamp, future, action, args)
                ActionType.ENABLE -> executionStatusService.enableExecutions(future, action, false)
                ActionType.DISABLE -> executionStatusService.enableExecutions(future, action, true)
                ActionType.LOCK -> lockService.acquireLock(future, action, triggerMessageSenderId)
                ActionType.UNLOCK -> lockService.unlock(future, action)
                ActionType.STATUS -> executionStatusService.showStatus(future, action)
                else -> throw UnsupportedOperationException("Action type ${actionType} is not supported")
            }
        } catch(e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }

    override fun buildRundeckApi(): RundeckApi {
        return Retrofit.Builder()
                .baseUrl(DriverSettings.deployment.baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()
                .create(RundeckApi::class.java)
    }
}
