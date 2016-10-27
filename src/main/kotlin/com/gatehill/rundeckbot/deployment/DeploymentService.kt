package com.gatehill.rundeckbot.deployment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.rundeckbot.config.Settings
import com.gatehill.rundeckbot.config.ConfigService
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

    private val logger = LogManager.getLogger(DeploymentService::class.java)!!
    private val settings = Settings()
    private val objectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Trigger the specified job with the given arguments.
     */
    fun triggerJob(job: ConfigService.JobConfig, executionArgs: Map<String, String>): CompletableFuture<ExecutionDetails> {
        val future = CompletableFuture<ExecutionDetails>()
        try {
            val jobId = job.jobId ?: throw RuntimeException("No ID found for job '${job.name}")

            val allArgs = executionArgs.plus(job.options ?: emptyMap())
            logger.info("Triggering job: {} ({}) with args: {}", job.name, jobId, allArgs)

            val rundeckApi = Retrofit.Builder()
                    .baseUrl(settings.deployment.baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .build()
                    .create(RundeckApi::class.java)

            val call = rundeckApi.runJob(
                    apiToken = settings.deployment.apiToken,
                    jobId = jobId,
                    executionOptions = ExecutionOptions(argString = buildArgString(allArgs))
            )

            call.enqueue(object : Callback<ExecutionDetails> {
                override fun onFailure(call: Call<ExecutionDetails>, t: Throwable) {
                    logger.error("Error triggering job: {} with args: {}", jobId, allArgs, t)
                    future.completeExceptionally(t)
                }

                override fun onResponse(call: Call<ExecutionDetails>, response: Response<ExecutionDetails>) {
                    if (response.isSuccessful) {
                        logger.info("Successfully triggered job: {} with args: {} - response: {}", jobId, allArgs, response.body())
                        future.complete(response.body())
                    } else {
                        logger.error("Unsuccessfully triggered job: {} with args: {} - response: {}", jobId, allArgs, response.errorBody().string())
                        future.completeExceptionally(RuntimeException(response.errorBody().toString()))
                    }
                }
            })

        } catch(e: Exception) {
            future.completeExceptionally(e)
        }

        return future
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
