package com.gatehill.rundeckbot.deployment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.rundeckbot.Config
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
     * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
     */
    interface RundeckApi {
        @POST("/api/14/job/{jobId}/run")
        fun runJob(@Header("Accept") accept: String = "application/json",
                   @Header("X-Rundeck-Auth-Token") authToken: String,
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

    val logger = LogManager.getLogger(DeploymentService::class.java)!!
    val config = Config()

    /**
     * Trigger the specified job with the given arguments.
     */
    fun triggerJob(jobId: String, jobArgs: Map<String, String>): CompletableFuture<ExecutionDetails> {
        logger.info("Triggering job: {} with args: {}", jobId, jobArgs)
        val future = CompletableFuture<ExecutionDetails>()

        try {
            val rundeckApi = Retrofit.Builder()
                    .baseUrl(config.deployment.baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create(ObjectMapper().registerKotlinModule()))
                    .build()
                    .create(RundeckApi::class.java)

            val call = rundeckApi.runJob(
                    authToken = config.deployment.authToken,
                    jobId = jobId,
                    executionOptions = ExecutionOptions(argString = buildArgString(jobArgs))
            )

            call.enqueue(object : Callback<ExecutionDetails> {
                override fun onFailure(call: Call<ExecutionDetails>, t: Throwable) {
                    logger.info("Error triggering job: {} with args: {}", jobId, jobArgs, t)
                    future.completeExceptionally(t)
                }

                override fun onResponse(call: Call<ExecutionDetails>, response: Response<ExecutionDetails>) {
                    if (response.isSuccessful) {
                        logger.info("Successfully triggered job: {} with args: {} - response: {}", jobId, jobArgs, response.body())
                        future.complete(response.body())
                    } else {
                        logger.error("Unsuccessfully triggered job: {} with args: {} - response: {}", jobId, jobArgs, response.errorBody().string())
                        future.completeExceptionally(RuntimeException(response.errorBody().toString()))
                    }
                }
            })

        } catch(e: Exception) {
            future.completeExceptionally(e)
        }

        return future
    }

    fun buildArgString(args: Map<String, String>): String {
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
