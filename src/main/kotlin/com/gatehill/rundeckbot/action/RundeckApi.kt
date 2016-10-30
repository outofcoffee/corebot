package com.gatehill.rundeckbot.action

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.*

/**
 * Models Rundeck REST API.
 */
interface RundeckApi {
    @POST("/api/14/action/{jobId}/run")
    fun runJob(@Header("Accept") accept: String = "application/json",
               @Header("X-Rundeck-Auth-Token") apiToken: String,
               @Path("jobId") jobId: String,
               @Body executionOptions: ActionService.ExecutionOptions): Call<ActionService.ExecutionDetails>

    @POST("/api/14/action/{jobId}/execution/enable")
    fun enableExecution(@Header("Accept") accept: String = "application/json",
                        @Header("X-Rundeck-Auth-Token") apiToken: String,
                        @Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("/api/14/action/{jobId}/execution/disable")
    fun disableExecution(@Header("Accept") accept: String = "application/json",
                         @Header("X-Rundeck-Auth-Token") apiToken: String,
                         @Path("jobId") jobId: String): Call<HashMap<String, Any>>
}
