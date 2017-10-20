package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.config.model.ActionConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ExecutionStatusService @Inject constructor(private val actionDriver: RundeckActionDriver,
                                                 private val lockService: LockService) {

    private val logger: Logger = LogManager.getLogger(RundeckActionDriver::class.java)

    fun enableExecutions(future: CompletableFuture<PerformActionResult>, action: ActionConfig, enable: Boolean) {
        logger.info("Setting action: {} with job ID: {} enabled status to {}", action.name, action.jobId, enable)

        val call: Call<HashMap<String, Any>> =
                if (enable) actionDriver.buildApiClient().enableExecution(
                        jobId = action.jobId
                ) else actionDriver.buildApiClient().disableExecution(
                        jobId = action.jobId
                )

        call.enqueue(object : Callback<HashMap<String, Any>> {
            override fun onFailure(call: Call<HashMap<String, Any>>, cause: Throwable) {
                logger.error("Error enabling action with job ID: {}", action.jobId, cause)
                future.completeExceptionally(cause)
            }

            override fun onResponse(call: Call<HashMap<String, Any>>, response: Response<HashMap<String, Any>>) {
                if (response.isSuccessful) {
                    logger.info("Successfully enabled action with job ID: {} - response: {}", action.jobId, response.body())
                    future.complete(PerformActionResult("I've ${if (enable) "enabled" else "disabled"} *${action.name}*."))
                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully enabled action with job ID: {} - response: {}", action.jobId, errorBody)
                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }
}
