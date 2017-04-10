package com.gatehill.corebot.driver.rundeck.action

import com.gatehill.corebot.driver.rundeck.model.ExecutionDetails
import com.gatehill.corebot.driver.rundeck.model.ExecutionInfo
import com.gatehill.corebot.driver.rundeck.model.ExecutionOptions
import com.gatehill.corebot.driver.rundeck.model.ExecutionOutput
import retrofit2.Call
import retrofit2.http.*
import java.util.*

/**
 * Models the Rundeck REST API.
 */
interface RundeckApi {
    @POST("/api/14/job/{jobId}/execution/enable")
    fun enableExecution(@Header("Accept") accept: String = "application/json",
                        @Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("/api/14/job/{jobId}/execution/disable")
    fun disableExecution(@Header("Accept") accept: String = "application/json",
                         @Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("/api/14/job/{jobId}/run")
    fun runJob(@Header("Accept") accept: String = "application/json",
               @Path("jobId") jobId: String,
               @Body executionOptions: ExecutionOptions): Call<ExecutionDetails>

    @GET("/api/14/execution/{executionId}")
    fun fetchExecutionInfo(@Header("Accept") accept: String = "application/json",
                           @Path("executionId") executionId: String): Call<ExecutionInfo>

    @GET("/api/14/execution/{executionId}/output")
    fun fetchExecutionOutput(@Header("Accept") accept: String = "application/json",
                                @Path("executionId") executionId: String): Call<ExecutionOutput>
}
