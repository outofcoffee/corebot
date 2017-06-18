package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.action.ActionOutcomeService
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.backend.jobs.service.BaseJobTriggerService
import com.gatehill.corebot.driver.model.ActionStatus
import com.gatehill.corebot.driver.model.TriggeredAction
import com.gatehill.corebot.backend.rundeck.model.ExecutionDetails
import com.gatehill.corebot.backend.rundeck.model.ExecutionInfo
import com.gatehill.corebot.backend.rundeck.model.ExecutionOptions
import com.gatehill.corebot.backend.rundeck.model.ExecutionOutput
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * Triggers Rundeck jobs and obtains execution status.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class RundeckJobTriggerService @Inject constructor(private val actionDriver: RundeckActionDriver,
                                                   lockService: LockService,
                                                   actionOutcomeService: ActionOutcomeService) : BaseJobTriggerService(lockService, actionOutcomeService) {

    private val logger: Logger = LogManager.getLogger(RundeckJobTriggerService::class.java)

    override fun triggerExecution(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                                  action: ActionConfig, args: Map<String, String>) {

        val call: Call<ExecutionDetails>
        try {
            call = actionDriver.buildApiClient().runJob(
                    jobId = action.jobId,
                    executionOptions = ExecutionOptions(
                            argString = buildArgString(args),
                            asUser = if (action.runAsTriggerUser) trigger.username else "")
            )
        } catch (e: Exception) {
            future.completeExceptionally(e)
            return
        }

        call.enqueue(object : Callback<ExecutionDetails> {
            override fun onFailure(call: Call<ExecutionDetails>, cause: Throwable) {
                logger.error("Error triggering job with ID: {} and args: {}", action.jobId, args, cause)
                future.completeExceptionally(cause)
            }

            override fun onResponse(call: Call<ExecutionDetails>, response: Response<ExecutionDetails>) {
                if (response.isSuccessful) {
                    val executionDetails = response.body()

                    logger.info("Successfully triggered job with ID: {} and args: {} - response: {}",
                            action.jobId, args, executionDetails)

                    val triggeredAction = TriggeredAction(executionDetails.id, executionDetails.permalink,
                            mapStatus(executionDetails.status))

                    checkStatus(trigger, action, triggeredAction, future)

                } else {
                    val errMsg = "Unsuccessfully triggered job [ID: ${action.jobId}, args: $args, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}"
                    logger.error(errMsg)
                    future.completeExceptionally(RuntimeException(errMsg))
                }
            }
        })
    }

    private fun buildArgString(args: Map<String, String>): String {
        val argString = StringBuilder()
        args.forEach {
            if (argString.isNotEmpty()) argString.append(" ")
            argString.append("-")
            argString.append(it.key)
            argString.append(" ")
            if (it.value.contains(" ")) {
                argString.append("\"").append(it.value).append("\"")
            } else {
                argString.append(it.value)
            }
        }
        return argString.toString()
    }

    private fun mapStatus(status: String): ActionStatus {
        return when (status) {
            "running" -> ActionStatus.RUNNING
            "succeeded" -> ActionStatus.SUCCEEDED
            "failed" -> ActionStatus.FAILED
            else -> ActionStatus.UNKNOWN
        }
    }

    override fun fetchExecutionInfo(trigger: TriggerContext, action: ActionConfig, executionId: Int, startTime: Long) {
        val call = actionDriver.buildApiClient().fetchExecutionInfo(
                executionId = executionId.toString()
        )

        call.enqueue(object : Callback<ExecutionInfo> {
            override fun onFailure(call: Call<ExecutionInfo>, cause: Throwable) =
                    handleStatusPollFailure(trigger, action, executionId, cause)

            override fun onResponse(call: Call<ExecutionInfo>, response: Response<ExecutionInfo>) {
                if (response.isSuccessful) {
                    val status = mapStatus(response.body().status)
                    processExecutionInfo(trigger, action, executionId, startTime, status)

                } else {
                    handleStatusPollError(trigger, action, executionId, response.errorBody().string())
                }
            }
        })
    }

    override fun fetchExecutionOutput(trigger: TriggerContext, action: ActionConfig, executionId: Int) {

        val call = actionDriver.buildApiClient().fetchExecutionOutput(
                executionId = executionId.toString()
        )

        call.enqueue(object : Callback<ExecutionOutput> {
            override fun onFailure(call: Call<ExecutionOutput>, cause: Throwable) =
                    handleOutputFailure(trigger, action, executionId, cause)

            override fun onResponse(call: Call<ExecutionOutput>, response: Response<ExecutionOutput>) {
                if (response.isSuccessful) {
                    var output = response.body().entries.map { it.log }.joinToString(separator = "\n") { it }
                    if (output.length > 0) {
                        sendOutput(trigger, action, executionId, output)
                    }

                } else {
                    handleOutputError(trigger, action, executionId, response.errorBody().string())
                }
            }
        })
    }
}
