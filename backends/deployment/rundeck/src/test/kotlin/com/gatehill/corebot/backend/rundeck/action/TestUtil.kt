package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.backend.rundeck.test.api.Project
import com.gatehill.corebot.backend.rundeck.test.api.RundeckAuthApi
import com.gatehill.corebot.backend.rundeck.test.api.RundeckCreateApi
import com.gatehill.corebot.test.KRundeckContainer
import com.gatehill.corebot.util.jsonMapper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.nio.file.Path
import java.util.concurrent.CompletableFuture


/**
 * Convenience functions for specifications.
 */
object TestUtil {
    fun fetchSessionId(rundeck: KRundeckContainer): CompletableFuture<String> {
        val future = CompletableFuture<String>()

        buildAuthApiClient<RundeckAuthApi>(rundeck).obtainSessionId().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                future.completeExceptionally(t)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val sessionId = response.headers().values("Set-Cookie")
                            .find { cookieHeader -> cookieHeader.startsWith("JSESSIONID=", true) }
                            ?.let { cookieValue ->
                                val semicolonPosition = cookieValue.indexOf(";")
                                val rawValue = if (semicolonPosition > -1) cookieValue.substring(0, semicolonPosition) else cookieValue
                                rawValue.split("=")[1]
                            }
                            ?: throw IllegalStateException("Missing JSESSIONID Set-Cookie header")

                    future.complete(sessionId)

                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            }
        })

        return future
    }

    fun fetchTokenFromSession(rundeck: KRundeckContainer, sessionId: String): String {
        val response = buildAuthApiClient<RundeckAuthApi>(rundeck)
                .createToken(buildSessionCookieHeader(sessionId)).execute().body()

        response?.let {
            return if (null == response.token && null != response.id) {
                response.id
            } else {
                response.token ?: throw IllegalStateException("Missing token")
            }
        } ?: throw IllegalStateException("Empty create token response")
    }

    private fun buildSessionCookieHeader(sessionId: String) = "JSESSIONID=$sessionId"

    fun createProject(rundeck: KRundeckContainer, sessionId: String, projectName: String) {
        val response = buildAuthApiClient<RundeckCreateApi>(rundeck)
                .createProject(buildSessionCookieHeader(sessionId), Project(projectName, emptyMap())).execute()

        checkResponseForError(response, "Error creating project: $projectName")
    }

    fun importJob(rundeck: KRundeckContainer, sessionId: String, projectName: String, jobFile: Path) {
        // Note: "application/yaml" is the MIME type to use, according to http://rundeck.org/docs/api/#importing-jobs
        val requestBody = RequestBody.create(MediaType.parse("application/yaml"), jobFile.toFile())

        val response = buildAuthApiClient<RundeckCreateApi>(rundeck)
                .importJob(buildSessionCookieHeader(sessionId), projectName, requestBody).execute()

        checkResponseForError(response, "Error importing project: $jobFile")
    }

    private inline fun <reified T> buildAuthApiClient(rundeck: KRundeckContainer): T {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
                .followRedirects(false)
                .addInterceptor(logging)
                .build()

        return Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(JacksonConverterFactory.create(jsonMapper))
                .baseUrl(rundeck.baseUrl).build()
                .create(T::class.java)
    }

    private fun checkResponseForError(response: Response<ResponseBody>, errorMessage: String) {
        if (response.code() < 200 || response.code() > 299)
            throw IllegalStateException("$errorMessage - HTTP ${response.code()}:\n${response.errorBody().string()}")
    }
}
