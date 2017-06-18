package com.gatehill.corebot.backend.jenkins.action

import com.gatehill.corebot.backend.jenkins.model.BuildDetails
import com.gatehill.corebot.backend.jenkins.model.QueuedItem
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Models the Jenkins API.
 */
interface JenkinsApi {
    @GET("crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)")
    fun fetchCrumb(): Call<ResponseBody>

    @POST("job/{jobName}/build")
    @FormUrlEncoded
    fun enqueueBuild(@Path("jobName") jobName: String,
                     @Field("token") token: String?): Call<Void>

    @POST("job/{jobName}/buildWithParameters")
    @FormUrlEncoded
    fun enqueueBuildWithParameters(@Path("jobName") jobName: String,
                                   @Field("token") token: String?,
                                   @FieldMap parameters: Map<String, String>): Call<Void>

    @GET("queue/item/{itemId}/api/json")
    fun fetchQueuedItem(@Header("Accept") accept: String = "application/json",
                        @Path("itemId") itemId: String,
                        @Query("token") token: String?): Call<QueuedItem>

    @GET("job/{jobName}/{buildId}/api/json")
    fun fetchBuild(@Header("Accept") accept: String = "application/json",
                   @Path("jobName") jobName: String,
                   @Path("buildId") buildId: String,
                   @Query("token") token: String?): Call<BuildDetails>
}
