package com.gatehill.rundeckbot.action

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.rundeckbot.chat.ActionType
import com.gatehill.rundeckbot.config.ActionConfig
import com.gatehill.rundeckbot.config.Settings
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
class ActionService {
    /**
     * Trigger a build with given options.
     */
    data class ExecutionOptions(val argString: String,
                                val logLevel: String = "INFO",
                                val asUser: String = "",
                                val filter: String = "")

    /**
     * Models the response to triggering a build.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ExecutionDetails(val id: Int,
                                val permalink: String,
                                val status: String)

    /**
     * A lock held on a job.
     */
    data class Lock(val owner: String)

    private val logger = LogManager.getLogger(ActionService::class.java)!!
    private val settings = Settings()
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val locks: MutableMap<ActionConfig, Lock> = mutableMapOf()

    /**
     * Trigger the specified action with the given arguments.
     */
    fun perform(actionType: ActionType, sender: String, action: ActionConfig,
                args: Map<String, String>): CompletableFuture<String> {

        val future = CompletableFuture<String>()
        try {
            when (actionType) {
                ActionType.TRIGGER -> trigger(future, action, args)
                ActionType.ENABLE -> enableExecutions(future, action, false)
                ActionType.DISABLE -> enableExecutions(future, action, true)
                ActionType.LOCK -> acquireLock(future, action, sender)
                ActionType.UNLOCK -> unlock(future, action)
                ActionType.STATUS -> showStatus(future, action)
            }
        } catch(e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }

    private fun trigger(future: CompletableFuture<String>, action: ActionConfig, args: Map<String, String>) {
        val jobId = action.jobId!!

        val lock = locks[action]
        if (null != lock) {
            future.completeExceptionally(IllegalStateException("The '${action.name}' action is locked by @${lock.owner}"))
            return
        }

        val allArgs = args.plus(action.options ?: emptyMap())
        logger.info("Triggering action: {} with job ID: {} and args: {}", action.name, jobId, allArgs)

        val call = buildRundeckApi().runJob(
                apiToken = settings.deployment.apiToken,
                jobId = jobId,
                executionOptions = ExecutionOptions(argString = buildArgString(allArgs))
        )

        call.enqueue(object : Callback<ExecutionDetails> {
            override fun onFailure(call: Call<ExecutionDetails>, t: Throwable) {
                logger.error("Error triggering job with ID: {} and args: {}", jobId, allArgs, t)
                future.completeExceptionally(t)
            }

            override fun onResponse(call: Call<ExecutionDetails>, response: Response<ExecutionDetails>) {
                if (response.isSuccessful) {
                    val executionDetails = response.body()
                    logger.info("Successfully triggered job with ID: {} and args: {} - response: {}", jobId, allArgs, executionDetails)
                    future.complete("""*Job status:* ${executionDetails.status}${if (executionDetails.status == "running") " :thumbsup:" else ""}
*Details:* ${executionDetails.permalink}""")

                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully triggered job with ID: {} and args: {} - response: {}", jobId, allArgs, errorBody)
                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }

    private fun enableExecutions(future: CompletableFuture<String>, action: ActionConfig, enable: Boolean) {
        logger.info("Setting action: {} with job ID: {} enabled status to {}", action.name, action.jobId, enable)

        val jobId = action.jobId!!
        val call: Call<HashMap<String, Any>>
        if (enable) {
            call = buildRundeckApi().enableExecution(
                    apiToken = settings.deployment.apiToken,
                    jobId = jobId
            )
        } else {
            call = buildRundeckApi().disableExecution(
                    apiToken = settings.deployment.apiToken,
                    jobId = jobId
            )
        }

        call.enqueue(object : Callback<HashMap<String, Any>> {
            override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                logger.error("Error enabling action with job ID: {}", action.jobId, t)
                future.completeExceptionally(t)
            }

            override fun onResponse(call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>) {
                if (response.isSuccessful) {
                    logger.info("Successfully enabled action with job ID: {} - response: {}", action.jobId, response.body())
                    future.complete("I've ${if (enable) "enabled" else "disabled"} *${action.name}*.")

                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully enabled action with job ID: {} - response: {}", action.jobId, errorBody)
                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }

    private fun acquireLock(future: CompletableFuture<String>, action: ActionConfig, sender: String) {
        val lock = locks[action]
        if (null != lock) {
            if (lock.owner == sender) {
                // already locked by self
                future.complete("BTW, you already had the lock for *${action.name}* :wink:")

            } else {
                // locked by someone else
                future.completeExceptionally(IllegalStateException(
                        "The lock for ${action.name} is already held by @${lock.owner}"))
            }

        } else {
            // acquire
            locks[action] = Lock(sender)
            future.complete("OK, you've locked :lock: *${action.name}*.")
        }
    }

    private fun unlock(future: CompletableFuture<String>, action: ActionConfig) {
        val lock = locks[action]
        if (null != lock) {
            // unlock
            locks.remove(action)
            future.complete("OK, you've unlocked :unlock: *${action.name}*.")

        } else {
            // already unlocked
            future.complete("BTW, *${action.name}* was already unlocked :wink:")
        }
    }

    private fun showStatus(future: CompletableFuture<String>, action: ActionConfig) {
        val msg = StringBuilder("Status of *${action.name}*: ")

        val lock = locks[action]
        if (null != lock) {
            msg.append("locked :lock: by @${lock.owner}")
        } else {
            msg.append("unlocked :unlock:")
        }

        future.complete(msg.toString())
    }

    private fun buildRundeckApi(): RundeckApi {
        return Retrofit.Builder()
                .baseUrl(settings.deployment.baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()
                .create(RundeckApi::class.java)
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
}
