package com.gatehill.corebot.driver.rundeck.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.chat.ChatLines
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.Settings
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.rundeck.config.DriverSettings
import com.gatehill.corebot.driver.rundeck.model.ExecutionDetails
import com.gatehill.corebot.driver.rundeck.model.ExecutionInfo
import com.gatehill.corebot.driver.rundeck.model.ExecutionOptions
import org.apache.logging.log4j.LogManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TriggerJobService @Inject constructor(private val actionDriver: RundeckActionDriver,
                                            private val lockService: LockService,
                                            private val sessionService: SessionService) {

    private val statusCheckInterval = 2000L
    private val jobStatusRunning = "running"
    private val jobStatusSucceeded = "succeeded"
    private val logger = LogManager.getLogger(TriggerJobService::class.java)!!

    /**
     * Trigger execution of a job, then poll for status updates.
     */
    fun trigger(channelId: String, triggerMessageTimestamp: String, future: CompletableFuture<PerformActionResult>,
                action: ActionConfig, args: Map<String, String>) {

        lockService.checkLock(action) { lock ->
            if (null != lock) {
                future.completeExceptionally(IllegalStateException(
                        "The '${action.name}' action is locked by <@${lock.owner}>"))
            } else {
                triggerExecution(channelId, triggerMessageTimestamp, future, action, args)
            }
        }
    }

    private fun triggerExecution(channelId: String, triggerMessageTimestamp: String, future: CompletableFuture<PerformActionResult>,
                                 action: ActionConfig, args: Map<String, String>) {

        val allArgs = args.plus(action.options.static ?: emptyMap())
        logger.info("Triggering action: {} with job ID: {} and args: {}", action.name, action.jobId, allArgs)

        val call = actionDriver.buildRundeckApi().runJob(
                apiToken = DriverSettings.deployment.apiToken,
                jobId = action.jobId,
                executionOptions = ExecutionOptions(argString = buildArgString(allArgs))
        )

        call.enqueue(object : Callback<ExecutionDetails> {
            override fun onFailure(call: Call<ExecutionDetails>, t: Throwable) {
                logger.error("Error triggering job with ID: {} and args: {}", action.jobId, allArgs, t)
                future.completeExceptionally(t)
            }

            override fun onResponse(call: Call<ExecutionDetails>, response: Response<ExecutionDetails>) {
                if (response.isSuccessful) {
                    val executionDetails = response.body()

                    logger.info("Successfully triggered job with ID: {} and args: {} - response: {}",
                            action.jobId, allArgs, executionDetails)

                    val reaction = if (arrayOf(jobStatusRunning, jobStatusSucceeded).contains(executionDetails.status)) " :thumbsup:" else ""
                    future.complete(PerformActionResult(
                            "Job #${executionDetails.id} status: _${executionDetails.status}_${reaction} (${executionDetails.permalink})", false))

                    if (executionDetails.status == jobStatusRunning) {
                        pollStatus(channelId, triggerMessageTimestamp, action, executionDetails.id)

                    } else {
                        // already finished
                        reactToFinalStatus(channelId, triggerMessageTimestamp, action, executionDetails.id, executionDetails.status)
                    }

                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully triggered job with ID: {} and args: {} - response: {}",
                            action.jobId, allArgs, errorBody)

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

    private fun pollStatus(channelId: String, triggerMessageTimestamp: String,
                           action: ActionConfig, executionId: Int) {

        val call = actionDriver.buildRundeckApi().fetchExecutionInfo(
                apiToken = DriverSettings.deployment.apiToken,
                executionId = executionId.toString()
        )

        fetchExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, call, System.currentTimeMillis())
    }

    private fun fetchExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                   executionId: Int, call: Call<ExecutionInfo>, startTime: Long) {

        Timer().schedule(object : TimerTask() {
            override fun run() {
                call.enqueue(object : Callback<ExecutionInfo> {
                    override fun onFailure(call: Call<ExecutionInfo>, t: Throwable) {
                        logger.error("Error checking for execution {} status", executionId, t)

                        sessionService.addReaction(channelId, triggerMessageTimestamp, "x")
                        sessionService.sendMessage(channelId,
                                "Error polling for *${action.name}* job status:\r\n```${t.message}```")
                    }

                    override fun onResponse(call: Call<ExecutionInfo>, response: Response<ExecutionInfo>) {
                        if (response.isSuccessful) {
                            processExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, call, startTime, response.body())

                        } else {
                            val errorBody = response.errorBody().string()
                            logger.error("Error checking for execution {} status: {}", executionId, errorBody)

                            sessionService.addReaction(channelId, triggerMessageTimestamp, "x")
                            sessionService.sendMessage(channelId,
                                    "Error polling for *${action.name}* job status:\r\n```${errorBody}```")
                        }
                    }
                })
            }
        }, statusCheckInterval)
    }

    private fun processExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                     executionId: Int, call: Call<ExecutionInfo>, startTime: Long, executionInfo: ExecutionInfo) {

        logger.info("Execution {} status: {}", executionId, executionInfo.status)

        if (executionInfo.status == jobStatusRunning) {
            if (System.currentTimeMillis() - startTime < Settings.deployment.executionTimeout) {
                // enough time left to retry
                fetchExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, call.clone(), startTime)

            } else {
                // timed out
                logger.error("Timed out checking for execution {} status after {}ms",
                        executionId, Settings.deployment.executionTimeout)

                sessionService.addReaction(channelId, triggerMessageTimestamp, "x")

                val timeoutSecs = TimeUnit.MILLISECONDS.toSeconds(Settings.deployment.executionTimeout.toLong())
                sessionService.sendMessage(channelId,
                        "Gave up polling for *${action.name}* job status after ${timeoutSecs} seconds.")
            }

        } else {
            reactToFinalStatus(channelId, triggerMessageTimestamp, action, executionId, executionInfo.status)
        }
    }

    private fun reactToFinalStatus(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                   executionId: Int, executionStatus: String) {

        val reaction: String
        val emoji: String
        when (executionStatus) {
            jobStatusSucceeded -> {
                reaction = ChatLines.goodNews()
                emoji = "white_check_mark"
            }
            else -> {
                reaction = ChatLines.badNews()
                emoji = "x"
            }
        }

        sessionService.addReaction(channelId, triggerMessageTimestamp, emoji)
        sessionService.sendMessage(channelId,
                "${reaction} *${action.name}* #${executionId} finished with status: _${executionStatus}_.")
    }
}
