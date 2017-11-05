package com.gatehill.corebot.backend.rundeck.test.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Creates objects in a Rundeck server.
 */
interface RundeckCreateApi {
    @POST("api/11/projects")
    @Headers("Accept: application/json")
    fun createProject(@Header("Cookie") cookieHeader: String,
                      @Body project: Project): Call<ResponseBody>

    @POST("api/14/project/{project}/jobs/import")
    @Headers("Accept: application/json")
    fun importJob(@Header("Cookie") cookieHeader: String,
                  @Path("project") project: String,
                  @Body body: RequestBody): Call<ResponseBody>
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Project(val name: String,
                   val config: Map<String, String>)
