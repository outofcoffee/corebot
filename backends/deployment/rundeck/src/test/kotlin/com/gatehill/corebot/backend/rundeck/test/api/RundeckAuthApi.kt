package com.gatehill.corebot.backend.rundeck.test.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Authenticates with a Rundeck server.
 */
interface RundeckAuthApi {
    @POST("j_security_check")
    @FormUrlEncoded
    fun obtainSessionId(@Field("j_username") username: String = "admin",
                        @Field("j_password") password: String = "admin"): Call<ResponseBody>

    @POST("api/11/tokens/{username}")
    @Headers("Accept: application/json")
    fun createToken(@Header("Cookie") cookieHeader: String,
                    @Path("username") username: String = "admin"): Call<UserToken>
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserToken(val id: String?,
                     val token: String?)
