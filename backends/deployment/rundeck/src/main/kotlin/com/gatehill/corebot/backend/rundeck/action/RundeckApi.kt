package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.backend.rundeck.model.ExecutionDetails
import com.gatehill.corebot.backend.rundeck.model.ExecutionInfo
import com.gatehill.corebot.backend.rundeck.model.ExecutionOptions
import com.gatehill.corebot.backend.rundeck.model.ExecutionOutput
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.HashMap

/**
 * Models the Rundeck REST API.
 */
interface RundeckApi {
    @POST("api/14/job/{jobId}/execution/enable")
    fun enableExecution(@Header("Accept") accept: String = "application/json",
                        @Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("api/14/job/{jobId}/execution/disable")
    fun disableExecution(@Header("Accept") accept: String = "application/json",
                         @Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("api/14/job/{jobId}/run")
    fun runJob(@Header("Accept") accept: String = "application/json",
               @Path("jobId") jobId: String,
               @Body executionOptions: ExecutionOptions): Call<ExecutionDetails>

    @GET("api/14/execution/{executionId}")
    fun fetchExecutionInfo(@Header("Accept") accept: String = "application/json",
                           @Path("executionId") executionId: String): Call<ExecutionInfo>

    @GET("api/14/execution/{executionId}/output")
    fun fetchExecutionOutput(@Header("Accept") accept: String = "application/json",
                             @Path("executionId") executionId: String): Call<ExecutionOutput>
}
