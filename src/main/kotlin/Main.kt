import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface RundeckApi {
    @POST("/api/14/job/{jobId}/run")
    fun runJob(@Header("Accept") accept: String = "application/json",
               @Header("Content-Type") contentType: String = "application/json",
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

fun main(args: Array<String>) {
    val baseUrl = if (args.size >= 1) args[0] else "http://localhost:4440"
    val authToken = if (args.size >= 2) args[1] else "XMFJoYF5bziYc5ZoCMM25t1hqNnDl5Ni"
    val jobId = if (args.size >= 3) args[2] else "9374f1c8-7b3f-4145-8556-6b55551fb60f"
    val jobArgs = mapOf(Pair("foo", "baz"))

    triggerJob(baseUrl, authToken, jobId, jobArgs)
}

private fun triggerJob(baseUrl: String, authToken: String, jobId: String, jobArgs: Map<String, String>) {
    val rundeckApi = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(ObjectMapper().registerKotlinModule()))
            .build()
            .create(RundeckApi::class.java)

    val executionDetails = ExecutionOptions(argString = buildArgString(jobArgs))
    val call = rundeckApi.runJob(
            authToken = authToken,
            jobId = jobId,
            executionOptions = executionDetails
    )

    call.enqueue(object : Callback<ExecutionDetails> {
        override fun onFailure(call: Call<ExecutionDetails>, t: Throwable) {
            throw t
        }

        override fun onResponse(call: Call<ExecutionDetails>, response: Response<ExecutionDetails>) {
            println(if (response.isSuccessful) response.body() else response.errorBody().string())
        }
    })
}

fun buildArgString(args: Map<String, String>): String {
    val argString = StringBuilder()
    args.forEach {
        argString.append("-")
        argString.append(it.key)
        argString.append(" ")
        argString.append(it.value)
    }
    return argString.toString()
}
