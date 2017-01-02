package com.gatehill.corebot.driver.jenkins.action

import com.gatehill.corebot.action.BaseJobTriggerService
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.model.ActionStatus
import com.gatehill.corebot.action.model.PerformActionResult
import com.gatehill.corebot.action.model.TriggeredAction
import com.gatehill.corebot.chat.ChatLines
import com.gatehill.corebot.chat.SessionService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.jenkins.config.DriverSettings
import com.gatehill.corebot.driver.jenkins.model.BuildDetails
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * Triggers Jenkins jobs and obtains job status.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class JenkinsJobTriggerService @Inject constructor(private val actionDriver: JenkinsActionDriver,
                                                   lockService: LockService,
                                                   private val sessionService: SessionService) : BaseJobTriggerService(lockService, sessionService) {

    private val logger: Logger = LogManager.getLogger(JenkinsJobTriggerService::class.java)

    override fun triggerExecution(channelId: String, triggerMessageTimestamp: String,
                                  future: CompletableFuture<PerformActionResult>,
                                  action: ActionConfig, args: Map<String, String>) {

        val apiClient: JenkinsApi
        val call: Call<Void>
        try {
            val headers = mutableMapOf<String, String>()
            obtainCsrfToken()?.let { headers.plusAssign(it) }

            apiClient = actionDriver.buildApiClient(headers)
            call = apiClient.enqueueBuild(
                    token = DriverSettings.deployment.apiToken,
                    jobName = action.jobId,
                    parameters = args
            )

        } catch(e: Exception) {
            future.completeExceptionally(RuntimeException("Error building API client or obtaining CSRF token", e))
            return
        }

        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, cause: Throwable) {
                logger.error("Error triggering job with ID: {} and args: {}", action.jobId, args, cause)
                future.completeExceptionally(cause)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    handleTriggerResponse(action, apiClient, args, channelId, future, response, triggerMessageTimestamp)

                } else {
                    val errorBody = response.errorBody().string()
                    logger.error("Unsuccessfully triggered job with ID: {} and args: {} - response: {}",
                            action.jobId, args, errorBody)

                    future.completeExceptionally(RuntimeException(errorBody))
                }
            }
        })
    }

    /**
     * Process the response to triggering a build.
     */
    private fun handleTriggerResponse(action: ActionConfig, apiClient: JenkinsApi, args: Map<String, String>,
                                      channelId: String, future: CompletableFuture<PerformActionResult>,
                                      response: Response<Void>, triggerMessageTimestamp: String) {

        when (response.code()) {
            201 -> {
                val queuedItemUrl: String? = response.headers()["Location"]
                if (queuedItemUrl == null) {
                    future.completeExceptionally(RuntimeException(
                            "No item was queued for triggered job with ID: ${action.jobId} and args: ${args}"))

                } else {
                    logger.debug("Queued item URL: $queuedItemUrl for job with ID: ${action.jobId} and args: ${args}")

                    // poll the queued item until reified into a build
                    processQueuedBuild(action, apiClient, args, channelId, future, triggerMessageTimestamp, queuedItemUrl)
                }
            }
            else -> {
                val errMsg = "Unsuccessfully triggered job with ID: ${action.jobId} and args: ${args} - response code: {response.code()}"
                logger.error(errMsg)
                future.completeExceptionally(RuntimeException(errMsg))
            }
        }
    }

    /**
     * Obtain CSRF token, if one can be provided by Jenkins.
     * See <a href="https://wiki.jenkins-ci.org/display/JENKINS/Remote+access+API#RemoteaccessAPI-CSRFProtection">docs</a>.
     */
    private fun obtainCsrfToken(): Pair<String, String>? {
        var tokenPair: Pair<String, String>? = null

        // this initial API client won't have an CSRF token
        val apiClient = actionDriver.buildApiClient()
        val response = apiClient.fetchCrumb().execute()

        when (response.code()) {
            404 -> {
                logger.debug("CSRF protection is disabled in Jenkins")
            }
            else -> {
                logger.debug("CSRF token obtained from Jenkins")
                val csrfToken = response.body().string()

                // Jenkins 1.x and 2.x support (see https://issues.jenkins-ci.org/browse/JENKINS-12875)
                arrayOf(".crumb", "Jenkins-Crumb").forEach {
                    if (csrfToken.startsWith("${it}:")) tokenPair = Pair(it, csrfToken.substring(it.length + 1))
                }

                tokenPair ?: throw IllegalStateException("Unable to parse CSRF token")
            }
        }

        return tokenPair
    }

    /**
     * Poll the queued item until reified into a build.
     */
    private fun processQueuedBuild(action: ActionConfig, apiClient: JenkinsApi, args: Map<String, String>,
                                   channelId: String, triggerResponseFuture: CompletableFuture<PerformActionResult>,
                                   triggerMessageTimestamp: String, queuedItemUrl: String) {

        // notify user that job is queued
        sessionService.sendMessage(channelId, "Build for *${action.name}* is queued - ${ChatLines.pleaseWait().toLowerCase()}...")

        // reify into build
        val fetchBuildIdFuture = fetchBuildIdFromQueuedItem(channelId, triggerMessageTimestamp, apiClient, queuedItemUrl)
        fetchBuildIdFuture.whenComplete { buildId, cause ->
            if (fetchBuildIdFuture.isCompletedExceptionally) {
                triggerResponseFuture.completeExceptionally(RuntimeException(
                        "Unable to fetch build ID from queued item: ${queuedItemUrl}", cause))

            } else {
                val executionDetails = apiClient.fetchBuild(
                        jobName = action.jobId,
                        buildId = buildId.toString(),
                        token = DriverSettings.deployment.apiToken
                ).execute().body()

                logger.info("Successfully triggered job with ID: {} and args: {} - response: {}",
                        action.jobId, args, executionDetails)

                val triggeredAction = TriggeredAction(executionDetails.number, executionDetails.url,
                        mapStatus(executionDetails))

                checkStatus(action, channelId, triggeredAction, triggerResponseFuture, triggerMessageTimestamp)
            }
        }
    }

    private fun mapStatus(buildDetails: BuildDetails): ActionStatus {
        return if (buildDetails.building)
            ActionStatus.RUNNING
        else if (buildDetails.result.equals("SUCCESS", ignoreCase = true))
            ActionStatus.SUCCEEDED
        else if (buildDetails.result.equals("FAILURE", ignoreCase = true))
            ActionStatus.FAILED
        else
            ActionStatus.UNKNOWN
    }

    private fun fetchBuildIdFromQueuedItem(channelId: String, triggerMessageTimestamp: String,
                                           apiClient: JenkinsApi, url: String): CompletableFuture<Int> {

        val future = CompletableFuture<Int>()

        val queuedItemUrl = if (url.endsWith('/')) url.substring(0..url.length - 2) else url
        val itemId = queuedItemUrl.substring(queuedItemUrl.lastIndexOf('/') + 1)

        pollQueuedItem(channelId, triggerMessageTimestamp, apiClient, future, itemId)

        return future
    }

    private fun pollQueuedItem(channelId: String, triggerMessageTimestamp: String,
                               apiClient: JenkinsApi, future: CompletableFuture<Int>, itemId: String,
                               startTime: Long = System.currentTimeMillis()) {

        val queuedItem = apiClient.fetchQueuedItem(
                itemId = itemId,
                token = DriverSettings.deployment.apiToken
        ).execute().body()

        queuedItem.executable?.number?.let {
            future.complete(queuedItem.executable.number)

        } ?: run {
            doUnlessTimedOut(channelId, startTime, triggerMessageTimestamp, "polling queued item #${itemId}") {
                pollQueuedItem(channelId, triggerMessageTimestamp, apiClient, future, itemId, startTime)
            }
        }
    }

    override fun fetchExecutionInfo(channelId: String, triggerMessageTimestamp: String, action: ActionConfig,
                                    executionId: Int, startTime: Long) {

        val call = actionDriver.buildApiClient().fetchBuild(
                jobName = action.jobId,
                buildId = executionId.toString(),
                token = DriverSettings.deployment.apiToken
        )

        call.enqueue(object : Callback<BuildDetails> {
            override fun onFailure(call: Call<BuildDetails>, cause: Throwable) =
                    handleStatusPollFailure(action, channelId, executionId, cause, triggerMessageTimestamp)

            override fun onResponse(call: Call<BuildDetails>, response: Response<BuildDetails>) {
                if (response.isSuccessful) {
                    val status = mapStatus(response.body())
                    processExecutionInfo(channelId, triggerMessageTimestamp, action, executionId, startTime, status)

                } else {
                    handleStatusPollError(action, channelId, executionId, triggerMessageTimestamp, response.errorBody().string())
                }
            }
        })
    }
}
