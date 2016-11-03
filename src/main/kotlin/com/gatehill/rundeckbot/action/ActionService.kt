package com.gatehill.rundeckbot.action

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.rundeckbot.action.model.PerformActionResult
import com.gatehill.rundeckbot.chat.ActionType
import com.gatehill.rundeckbot.config.Settings
import com.gatehill.rundeckbot.config.model.ActionConfig
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import org.apache.logging.log4j.LogManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ActionService {
    private val logger = LogManager.getLogger(ActionService::class.java)!!
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val executionService by lazy { ExecutionService }
    private val lockService by lazy { LockService }

    /**
     * Trigger the specified action with the given arguments.
     */
    fun perform(session: SlackSession, event: SlackMessagePosted, actionType: ActionType,
                action: ActionConfig, args: Map<String, String>): CompletableFuture<PerformActionResult> {

        val future = CompletableFuture<PerformActionResult>()
        try {
            when (actionType) {
                ActionType.TRIGGER -> executionService.trigger(session, event, future, action, args)
                ActionType.ENABLE -> enableExecutions(future, action, false)
                ActionType.DISABLE -> enableExecutions(future, action, true)
                ActionType.LOCK -> lockService.acquireLock(future, action, event.sender)
                ActionType.UNLOCK -> lockService.unlock(future, action)
                ActionType.STATUS -> showStatus(future, action)
                else -> throw UnsupportedOperationException("Action type ${actionType} is not supported")
            }
        } catch(e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }

    fun buildRundeckApi(): RundeckApi {
        return Retrofit.Builder()
                .baseUrl(Settings.deployment.baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()
                .create(RundeckApi::class.java)
    }

    private fun enableExecutions(future: CompletableFuture<PerformActionResult>, action: ActionConfig, enable: Boolean) {
        logger.info("Setting action: {} with job ID: {} enabled status to {}", action.name, action.jobId, enable)

        val call: Call<HashMap<String, Any>> =
                if (enable) buildRundeckApi().enableExecution(
                        apiToken = Settings.deployment.apiToken,
                        jobId = action.jobId
                ) else buildRundeckApi().disableExecution(
                        apiToken = Settings.deployment.apiToken,
                        jobId = action.jobId
                )

        call.enqueue(object : Callback<HashMap<String, Any>> {
            override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                logger.error("Error enabling action with job ID: {}", action.jobId, t)
                future.completeExceptionally(t)
            }

            override fun onResponse(call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>) {
                if (response.isSuccessful) {
                    logger.info("Successfully enabled action with job ID: {} - response: {}", action.jobId, response.body())
                    future.complete(PerformActionResult("I've ${if (enable) "enabled" else "disabled"} *${action.name}*."))
                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully enabled action with job ID: {} - response: {}", action.jobId, errorBody)
                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }

    private fun showStatus(future: CompletableFuture<PerformActionResult>, action: ActionConfig) {
        val msg = StringBuilder("Status of *${action.name}*: ")

        lockService.checkLock(action) { lock ->
            if (null != lock) {
                msg.append("locked :lock: by <@${lock.owner.id}>")
            } else {
                msg.append("unlocked :unlock:")
            }

            future.complete(PerformActionResult(msg.toString()))
        }
    }
}
