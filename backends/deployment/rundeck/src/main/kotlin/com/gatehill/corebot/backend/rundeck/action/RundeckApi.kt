package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.backend.rundeck.model.ExecutionDetails
import com.gatehill.corebot.backend.rundeck.model.ExecutionInfo
import com.gatehill.corebot.backend.rundeck.model.ExecutionOptions
import com.gatehill.corebot.backend.rundeck.model.ExecutionOutput
import com.gatehill.corebot.backend.rundeck.model.RundeckJob
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.HashMap

/**
 * Models the Rundeck REST API.
 */
interface RundeckApi {
    @GET("api/14/project/{projectId}/jobs")
    fun findProjectJob(@Path("projectId") projectId: String,
                       @Query("jobExactFilter") jobExactFilter: String): Call<RundeckJob>

    @POST("api/14/job/{jobId}/execution/enable")
    @Headers("Accept: application/json")
    fun enableExecution(@Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("api/14/job/{jobId}/execution/disable")
    @Headers("Accept: application/json")
    fun disableExecution(@Path("jobId") jobId: String): Call<HashMap<String, Any>>

    @POST("api/14/job/{jobId}/run")
    @Headers("Accept: application/json")
    fun runJob(@Path("jobId") jobId: String,
               @Body executionOptions: ExecutionOptions): Call<ExecutionDetails>

    @GET("api/14/execution/{executionId}")
    @Headers("Accept: application/json")
    fun fetchExecutionInfo(@Path("executionId") executionId: String): Call<ExecutionInfo>

    @GET("api/14/execution/{executionId}/output")
    @Headers("Accept: application/json")
    fun fetchExecutionOutput(@Path("executionId") executionId: String): Call<ExecutionOutput>
}
