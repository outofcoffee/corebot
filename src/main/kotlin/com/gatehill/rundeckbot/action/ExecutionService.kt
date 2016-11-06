package com.gatehill.rundeckbot.action

import com.gatehill.rundeckbot.action.model.ExecutionDetails
import com.gatehill.rundeckbot.action.model.ExecutionInfo
import com.gatehill.rundeckbot.action.model.ExecutionOptions
import com.gatehill.rundeckbot.action.model.PerformActionResult
import com.gatehill.rundeckbot.chat.ChatLines
import com.gatehill.rundeckbot.config.Settings
import com.gatehill.rundeckbot.config.model.ActionConfig
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import org.apache.logging.log4j.LogManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ExecutionService {
    private val statusCheckInterval = 2000L
    private val jobStatusRunning = "running"
    private val jobStatusSucceeded = "succeeded"
    private val logger = LogManager.getLogger(ExecutionService::class.java)!!
    private val actionService by lazy { ActionService }
    private val lockService by lazy { LockService }

    /**
     * Trigger execution of a job, then poll for status updates.
     */
    fun trigger(session: SlackSession, event: SlackMessagePosted, future: CompletableFuture<PerformActionResult>,
                action: ActionConfig, args: Map<String, String>) {

        lockService.checkLock(action) { lock ->
            if (null != lock) {
                future.completeExceptionally(IllegalStateException(
                        "The '${action.name}' action is locked by <@${lock.owner.id}>"))
            } else {
                triggerExecution(session, event, future, action, args)
            }
        }
    }

    private fun triggerExecution(session: SlackSession, event: SlackMessagePosted, future: CompletableFuture<PerformActionResult>,
                                 action: ActionConfig, args: Map<String, String>) {

        val allArgs = args.plus(action.options.static ?: emptyMap())
        logger.info("Triggering action: {} with job ID: {} and args: {}", action.name, action.jobId, allArgs)

        val call = actionService.buildRundeckApi().runJob(
                apiToken = Settings.deployment.apiToken,
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
                        pollStatus(session, event, action, executionDetails.id)

                    } else {
                        // already finished
                        reactToFinalStatus(session, event, action, executionDetails.id, executionDetails.status)
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
            if (argString.length > 0) argString.append(" ")
            argString.append("-")
            argString.append(it.key)
            argString.append(" ")
            argString.append(it.value)
        }
        return argString.toString()
    }

    private fun pollStatus(session: SlackSession, event: SlackMessagePosted,
                           action: ActionConfig, executionId: Int) {

        val call = actionService.buildRundeckApi().fetchExecutionInfo(
                apiToken = Settings.deployment.apiToken,
                executionId = executionId.toString()
        )

        fetchExecutionInfo(session, event, action, executionId, call, System.currentTimeMillis())
    }

    private fun fetchExecutionInfo(session: SlackSession, event: SlackMessagePosted, action: ActionConfig,
                                   executionId: Int, call: Call<ExecutionInfo>, startTime: Long) {

        Timer().schedule(object : TimerTask() {
            override fun run() {
                call.enqueue(object : Callback<ExecutionInfo> {
                    override fun onFailure(call: Call<ExecutionInfo>, t: Throwable) {
                        logger.error("Error checking for execution {} status", executionId, t)

                        session.addReactionToMessage(event.channel, event.timeStamp, "x")
                        session.sendMessage(event.channel,
                                "Error polling for *${action.name}* job status:\r\n```${t.message}```")
                    }

                    override fun onResponse(call: Call<ExecutionInfo>, response: Response<ExecutionInfo>) {
                        if (response.isSuccessful) {
                            processExecutionInfo(session, event, action, executionId, call, startTime, response.body())

                        } else {
                            val errorBody = response.errorBody().string()
                            logger.error("Error checking for execution {} status: {}", executionId, errorBody)

                            session.addReactionToMessage(event.channel, event.timeStamp, "x")
                            session.sendMessage(event.channel,
                                    "Error polling for *${action.name}* job status:\r\n```${errorBody}```")
                        }
                    }
                })
            }
        }, statusCheckInterval)
    }

    private fun processExecutionInfo(session: SlackSession, event: SlackMessagePosted, action: ActionConfig,
                                     executionId: Int, call: Call<ExecutionInfo>, startTime: Long, executionInfo: ExecutionInfo) {

        logger.info("Execution {} status: {}", executionId, executionInfo.status)

        if (executionInfo.status == jobStatusRunning) {
            if (System.currentTimeMillis() - startTime < Settings.deployment.executionTimeout) {
                // enough time left to retry
                fetchExecutionInfo(session, event, action, executionId, call.clone(), startTime)

            } else {
                // timed out
                logger.error("Timed out checking for execution {} status after {}ms",
                        executionId, Settings.deployment.executionTimeout)

                session.addReactionToMessage(event.channel, event.timeStamp, "x")

                val timeoutSecs = TimeUnit.MILLISECONDS.toSeconds(Settings.deployment.executionTimeout.toLong())
                session.sendMessage(event.channel,
                        "Gave up polling for *${action.name}* job status after ${timeoutSecs} seconds.")
            }

        } else {
            reactToFinalStatus(session, event, action, executionId, executionInfo.status)
        }
    }

    private fun reactToFinalStatus(session: SlackSession, event: SlackMessagePosted, action: ActionConfig,
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

        session.addReactionToMessage(event.channel, event.timeStamp, emoji)
        session.sendMessage(event.channel,
                "${reaction} *${action.name}* #${executionId} finished with status: _${executionStatus}_.")
    }
}
