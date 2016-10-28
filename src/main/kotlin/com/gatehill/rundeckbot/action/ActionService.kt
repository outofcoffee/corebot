package com.gatehill.rundeckbot.action

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.rundeckbot.action.ActionType
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.JobConfig
import com.gatehill.rundeckbot.config.Settings
import org.apache.logging.log4j.LogManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ActionService {
    /**
     * Represents the RPC to trigger a job.
     */
    interface RundeckApi {
        @POST("/api/14/job/{jobId}/run")
        fun runJob(@Header("Accept") accept: String = "application/json",
                   @Header("X-Rundeck-Auth-Token") apiToken: String,
                   @Path("jobId") jobId: String,
                   @Body executionOptions: ExecutionOptions): Call<ExecutionDetails>

        @POST("/api/14/job/{jobId}/execution/enable")
        fun enableExecution(@Header("Accept") accept: String = "application/json",
                            @Header("X-Rundeck-Auth-Token") apiToken: String,
                            @Path("jobId") jobId: String): Call<HashMap<String, Any>>

        @POST("/api/14/job/{jobId}/execution/disable")
        fun disableExecution(@Header("Accept") accept: String = "application/json",
                             @Header("X-Rundeck-Auth-Token") apiToken: String,
                             @Path("jobId") jobId: String): Call<HashMap<String, Any>>
    }

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

    data class Lock(val owner: String)

    private val locks: MutableMap<String, Lock> = mutableMapOf()

    private val logger = LogManager.getLogger(ActionService::class.java)!!
    private val settings = Settings()
    private val objectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Trigger the specified job with the given arguments.
     */
    fun perform(action: com.gatehill.rundeckbot.action.ActionType, sender: String, job: JobConfig,
                executionArgs: Map<String, String>): CompletableFuture<String> {

        val future = CompletableFuture<String>()
        try {
            when (action) {
                com.gatehill.rundeckbot.action.ActionType.TRIGGER -> trigger(future, job, executionArgs)
                com.gatehill.rundeckbot.action.ActionType.ENABLE -> enableExecutions(future, job, false)
                com.gatehill.rundeckbot.action.ActionType.DISABLE -> enableExecutions(future, job, true)
                com.gatehill.rundeckbot.action.ActionType.LOCK -> acquireLock(future, job, sender)
                com.gatehill.rundeckbot.action.ActionType.UNLOCK -> unlock(future, job)
                com.gatehill.rundeckbot.action.ActionType.STATUS -> showStatus(future, job)
            }
        } catch(e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }

    private fun trigger(future: CompletableFuture<String>, job: JobConfig, executionArgs: Map<String, String>) {
        val jobId = job.jobId!!

        val lock = locks[jobId]
        if (null != lock) {
            future.completeExceptionally(IllegalStateException("The '${job.name}' job is locked by @${lock.owner}"))
            return
        }

        val args = executionArgs.plus(job.options ?: emptyMap())
        logger.info("Triggering job: {} ({}) with args: {}", job.name, jobId, args)

        val call = buildRundeckApi().runJob(
                apiToken = settings.deployment.apiToken,
                jobId = jobId,
                executionOptions = ExecutionOptions(argString = buildArgString(args))
        )

        call.enqueue(object : Callback<ExecutionDetails> {
            override fun onFailure(call: Call<ExecutionDetails>, t: Throwable) {
                logger.error("Error triggering job: {} with args: {}", jobId, args, t)
                future.completeExceptionally(t)
            }

            override fun onResponse(call: Call<ExecutionDetails>, response: Response<ExecutionDetails>) {
                if (response.isSuccessful) {
                    val executionDetails = response.body()
                    logger.info("Successfully triggered job: {} with args: {} - response: {}", jobId, args, executionDetails)
                    future.complete("""*Job status:* ${executionDetails.status}${ if (executionDetails.status == "running") " :thumbsup:" else "" }
*Details:* ${executionDetails.permalink}""")

                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully triggered job: {} with args: {} - response: {}", jobId, args, errorBody)
                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }

    private fun enableExecutions(future: CompletableFuture<String>, job: JobConfig, enable : Boolean) {
        logger.info("Setting job: {} ({}) enabled to {}", job.name, job.jobId, enable)

        val call: Call<HashMap<String, Any>>
        if (enable) {
            call = buildRundeckApi().enableExecution(
                    apiToken = settings.deployment.apiToken,
                    jobId = job.jobId!!
            )
        } else {
            call = buildRundeckApi().disableExecution(
                    apiToken = settings.deployment.apiToken,
                    jobId = job.jobId!!
            )
        }

        call.enqueue(object : Callback<HashMap<String, Any>> {
            override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                logger.error("Error enabling job: {}", job.jobId, t)
                future.completeExceptionally(t)
            }

            override fun onResponse(call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>) {
                if (response.isSuccessful) {
                    logger.info("Successfully enabled job: {} - response: {}", job.jobId, response.body())
                    future.complete("I've ${ if (enable) "enabled" else "disabled" } *${job.name}*.")

                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully enabled job: {} - response: {}", job.jobId, errorBody)
                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }

    private fun acquireLock(future: CompletableFuture<String>, job: JobConfig, sender: String) {
        val jobId = job.jobId!!

        val lock = locks[jobId]
        if (null != lock) {
            if (lock.owner == sender) {
                // already locked by self
                future.complete("BTW, you already had the lock for *${job.name}* :wink:")

            } else {
                // locked by someone else
                future.completeExceptionally(IllegalStateException(
                        "The lock for ${job.name} is already held by @${lock.owner}"))
            }

        } else{
            // acquire
            locks[jobId] = Lock(sender)
            future.complete("OK, you've locked :lock: *${job.name}*.")
        }
    }

    private fun unlock(future: CompletableFuture<String>, job: JobConfig) {
        val jobId = job.jobId!!

        val lock = locks[jobId]
        if (null != lock) {
            // unlock
            locks.remove(jobId)
            future.complete("OK, you've unlocked :unlock: *${job.name}*.")

        } else{
            // already unlocked
            future.complete("BTW, *${job.name}* was already unlocked :wink:")
        }
    }

    private fun showStatus(future: CompletableFuture<String>, job: JobConfig) {
        val  msg = StringBuilder("Status of *${job.name}*: ")

        val lock = locks[job.jobId!!]
        if (null != lock) {
            msg.append("locked :lock: by @${lock.owner}")
        } else{
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
