package com.gatehill.corebot.driver.rundeck.action

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.gatehill.corebot.driver.rundeck.model.ExecutionDetails
import com.gatehill.corebot.driver.rundeck.model.ExecutionInfo
import com.gatehill.corebot.driver.rundeck.model.ExecutionOptions
import retrofit2.Call
import retrofit2.http.*
import java.util.*

/**
 * Models the Rundeck REST API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
interface RundeckApi {
    @POST("/api/14/job/{jobId}/execution/enable")
    fun enableExecution(@Header("Accept") accept: String = "application/json",
                        @Header("X-Rundeck-Auth-Token") apiToken: String,
                        @Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("/api/14/job/{jobId}/execution/disable")
    fun disableExecution(@Header("Accept") accept: String = "application/json",
                         @Header("X-Rundeck-Auth-Token") apiToken: String,
                         @Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("/api/14/job/{jobId}/run")
    fun runJob(@Header("Accept") accept: String = "application/json",
               @Header("X-Rundeck-Auth-Token") apiToken: String,
               @Path("jobId") jobId: String,
               @Body executionOptions: ExecutionOptions): Call<ExecutionDetails>

    @GET("/api/14/execution/{executionId}")
    fun fetchExecutionInfo(@Header("Accept") accept: String = "application/json",
                           @Header("X-Rundeck-Auth-Token") apiToken: String,
                           @Path("executionId") executionId: String): Call<ExecutionInfo>
}
