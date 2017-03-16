package com.gatehill.corebot.driver.rundeck.action

import com.gatehill.corebot.action.BaseTriggerJobService
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.model.ActionStatus
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggeredAction
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.rundeck.model.ExecutionDetails
import com.gatehill.corebot.driver.rundeck.model.ExecutionInfo
import com.gatehill.corebot.driver.rundeck.model.ExecutionOptions
import com.gatehill.corebot.driver.rundeck.model.ExecutionOutput
import org.apache.logging.log4j.LogManager
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
class RundeckTriggerJobService @Inject constructor(private val actionDriver: RundeckActionDriver,
                                                   lockService: LockService,
                                                   sessionService: SessionService) : BaseTriggerJobService(lockService, sessionService) {

    private val logger = LogManager.getLogger(RundeckTriggerJobService::class.java)!!

    override fun triggerExecution(channelId: String, triggerMessageTimestamp: String,
                                  future: CompletableFuture<PerformActionResult>,
                                  action: ActionConfig, triggerMessageSenderName: String, args: Map<String, String>) {

        val call: Call<ExecutionDetails>
        try {
            call = actionDriver.buildApiClient().runJob(
                    jobId = action.jobId,
                    executionOptions = ExecutionOptions(argString = buildArgString(args),
                            asUser = triggerMessageSenderName)
            )
        } catch(e: Exception) {
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

                    checkStatus(action, channelId, triggeredAction, future, triggerMessageTimestamp)

                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully triggered job with ID: {} and args: {} - response: {}",
                            action.jobId, args, errorBody)

                    future.completeExceptionally(RuntimeException(errorBody))
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
            argString.append(it.value)
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

    override fun fetchExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                    executionId: Int, startTime: Long) {

        val call = actionDriver.buildApiClient().fetchExecutionInfo(
                executionId = executionId.toString()
        )

        call.enqueue(object : Callback<ExecutionInfo> {
            override fun onFailure(call: Call<ExecutionInfo>, cause: Throwable) =
                    handleStatusPollFailure(action, channelId, executionId, cause, triggerMessageTimestamp)

            override fun onResponse(call: Call<ExecutionInfo>, response: Response<ExecutionInfo>) {
                if (response.isSuccessful) {
                    val status = mapStatus(response.body().status)
                    processExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, startTime, status)

                } else {
                    handleStatusPollError(action, channelId, executionId, triggerMessageTimestamp, response.errorBody().string())
                }
            }
        })
    }

    override fun fetchExecutionOutput(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                      executionId: Int) {

        val call = actionDriver.buildApiClient().fetchExecutionOutput(
                executionId = executionId.toString()
        )

        call.enqueue(object : Callback<ExecutionOutput> {
            override fun onFailure(call: Call<ExecutionOutput>, cause: Throwable) =
                    handleOutputFailure(action, channelId, executionId, cause, triggerMessageTimestamp)

            override fun onResponse(call: Call<ExecutionOutput>, response: Response<ExecutionOutput>) {
                if (response.isSuccessful) {
                    var output: String = ""
                    for (entry in response.body().entries){
                        output += entry.log + "\n"
                    }
                    if (output.length > 0) {
                        sendOutput(channelId, action, executionId, output)
                    }

                } else {
                    handleOutputError(action, channelId, executionId, triggerMessageTimestamp, response.errorBody().string())
                }
            }
        })
    }
}
