package com.gatehill.rundeckbot.deployment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.rundeckbot.config.ConfigService
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
class DeploymentService {
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

    private val logger = LogManager.getLogger(DeploymentService::class.java)!!
    private val settings = Settings()
    private val objectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Trigger the specified job with the given arguments.
     */
    fun perform(action: ConfigService.TaskAction, sender: String, job: ConfigService.JobConfig,
                executionArgs: Map<String, String>): CompletableFuture<ExecutionDetails> {

        val future = CompletableFuture<ExecutionDetails>()
        try {
            when (action) {
                ConfigService.TaskAction.TRIGGER -> trigger(future, job, executionArgs)
                ConfigService.TaskAction.ENABLE -> enableExecutions(future, job, false)
                ConfigService.TaskAction.DISABLE -> enableExecutions(future, job, true)
                ConfigService.TaskAction.LOCK -> acquireLock(future, job, sender)
                ConfigService.TaskAction.UNLOCK -> unlock(future, job)
            }
        } catch(e: Exception) {
            future.completeExceptionally(e)
        }
        return future
    }

    private fun trigger(future: CompletableFuture<ExecutionDetails>, job: ConfigService.JobConfig, executionArgs: Map<String, String>) {
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
                    logger.info("Successfully triggered job: {} with args: {} - response: {}", jobId, args, response.body())
                    future.complete(response.body())
                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully triggered job: {} with args: {} - response: {}", jobId, args, errorBody)
                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }

    private fun enableExecutions(future: CompletableFuture<ExecutionDetails>, job: ConfigService.JobConfig, enable : Boolean) {
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

                    // TODO improve status reporting
                    future.complete(ExecutionDetails(0, "", ""))

                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully enabled job: {} - response: {}", job.jobId, errorBody)
                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }

    private fun acquireLock(future: CompletableFuture<ExecutionDetails>, job: ConfigService.JobConfig, sender: String) {
        val jobId = job.jobId!!

        val lock = locks[jobId]
        if (null != lock) {
            // already locked
            future.completeExceptionally(IllegalStateException("Lock for ${job.name} is already owned by @${lock.owner}"))

        } else{
            // acquire
            locks[jobId] = Lock(sender)

            // TODO improve status reporting
            future.complete(ExecutionDetails(0, "", ""))
        }
    }

    private fun unlock(future: CompletableFuture<ExecutionDetails>, job: ConfigService.JobConfig) {
        val jobId = job.jobId!!

        val lock = locks[jobId]
        if (null != lock) {
            // unlock
            locks.remove(jobId)

            // TODO improve status reporting
            future.complete(ExecutionDetails(0, "", ""))

        } else{
            // already unlocked
            // TODO improve status reporting
            future.complete(ExecutionDetails(0, "", ""))
        }
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
