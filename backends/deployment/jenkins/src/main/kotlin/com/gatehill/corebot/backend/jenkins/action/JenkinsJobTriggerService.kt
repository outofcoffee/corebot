package com.gatehill.corebot.backend.jenkins.action

import com.gatehill.corebot.action.ActionOutcomeService
import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.operation.model.PerformActionResult
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.backend.jenkins.config.DriverSettings
import com.gatehill.corebot.backend.jenkins.model.BuildDetails
import com.gatehill.corebot.backend.jobs.service.BaseJobTriggerService
import com.gatehill.corebot.driver.model.ActionStatus
import com.gatehill.corebot.driver.model.TriggeredAction
import com.gatehill.corebot.util.onException
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
                                                   private val actionOutcomeService: ActionOutcomeService) : BaseJobTriggerService(lockService, actionOutcomeService) {

    private val logger: Logger = LogManager.getLogger(JenkinsJobTriggerService::class.java)

    override fun triggerExecution(trigger: TriggerContext, future: CompletableFuture<PerformActionResult>,
                                  action: ActionConfig, args: Map<String, String>) {

        val apiClient: JenkinsApi
        val call: Call<Void>
        try {
            val headers = mutableMapOf<String, String>()
            obtainCsrfToken()?.let { headers.plusAssign(it) }

            apiClient = actionDriver.buildApiClient(headers)

            if (args.isEmpty()) {
                call = apiClient.enqueueBuild(
                        token = DriverSettings.deployment.apiToken,
                        jobName = action.jobId
                )
            } else {
                call = apiClient.enqueueBuildWithParameters(
                        token = DriverSettings.deployment.apiToken,
                        jobName = action.jobId,
                        parameters = args
                )
            }

        } catch (e: Exception) {
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
                    handleTriggerResponse(call, trigger, action, apiClient, args, future, response)

                } else {
                    val errMsg = "Unsuccessfully triggered job [ID: ${action.jobId}, args: $args, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}"
                    logger.error(errMsg)
                    future.completeExceptionally(RuntimeException(errMsg))
                }
            }
        })
    }

    /**
     * Process the response to triggering a build.
     */
    private fun handleTriggerResponse(call: Call<Void>, trigger: TriggerContext, action: ActionConfig, apiClient: JenkinsApi,
                                      args: Map<String, String>, future: CompletableFuture<PerformActionResult>,
                                      response: Response<Void>) {

        when (response.code()) {
            201 -> {
                val queuedItemUrl: String? = response.headers()["Location"]
                if (queuedItemUrl == null) {
                    future.completeExceptionally(RuntimeException(
                            "No item was queued for triggered job with ID: ${action.jobId} and args: $args"))

                } else {
                    logger.debug("Queued item URL: $queuedItemUrl for job with ID: ${action.jobId} and args: $args")

                    // poll the queued item until reified into a build
                    processQueuedBuild(trigger, action, apiClient, args, future, queuedItemUrl)
                }
            }
            else -> {
                val errMsg = "Unsuccessfully triggered job [ID: ${action.jobId}, args: $args, request URL: ${call.request().url()}, response code: ${response.code()}] response body: ${response.errorBody().string()}"
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
            401 -> throw IllegalStateException("Jenkins authentication failed")
            404 -> logger.debug("CSRF protection is disabled in Jenkins")
            200 -> {
                logger.debug("CSRF token obtained from Jenkins")
                val csrfToken = response.body().string()

                // Jenkins 1.x and 2.x support (see https://issues.jenkins-ci.org/browse/JENKINS-12875)
                arrayOf(".crumb", "Jenkins-Crumb").forEach {
                    if (csrfToken.startsWith("$it:")) tokenPair = Pair(it, csrfToken.substring(it.length + 1))
                }

                tokenPair ?: throw IllegalStateException("Unable to parse CSRF token")
            }
            else -> throw RuntimeException("Jenkins returned HTTP ${response.code()} ${response.message()}")
        }

        return tokenPair
    }

    /**
     * Poll the queued item until reified into a build.
     */
    private fun processQueuedBuild(trigger: TriggerContext, action: ActionConfig, apiClient: JenkinsApi,
                                   args: Map<String, String>, triggerResponseFuture: CompletableFuture<PerformActionResult>,
                                   queuedItemUrl: String) {

        // notify user that job is queued
        actionOutcomeService.notifyQueued(trigger, action)

        // reify into build
        fetchBuildIdFromQueuedItem(trigger, action, apiClient, queuedItemUrl).thenAccept { buildId ->
            val executionDetails = apiClient.fetchBuild(
                    jobName = action.jobId,
                    buildId = buildId.toString(),
                    token = DriverSettings.deployment.apiToken
            ).execute().body()

            logger.info("Successfully triggered job with ID: {} and args: {} - response: {}",
                    action.jobId, args, executionDetails)

            val triggeredAction = TriggeredAction(executionDetails.number, executionDetails.url,
                    mapStatus(executionDetails))

            checkStatus(trigger, action, triggeredAction, triggerResponseFuture)

        }.onException { ex ->
            triggerResponseFuture.completeExceptionally(RuntimeException(
                    "Unable to fetch build ID from queued item: $queuedItemUrl", ex))
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

    private fun fetchBuildIdFromQueuedItem(trigger: TriggerContext, action: ActionConfig, apiClient: JenkinsApi,
                                           url: String): CompletableFuture<Int> {

        val future = CompletableFuture<Int>()

        val queuedItemUrl = if (url.endsWith('/')) url.substring(0..url.length - 2) else url
        val itemId = queuedItemUrl.substring(queuedItemUrl.lastIndexOf('/') + 1)

        pollQueuedItem(trigger, action, apiClient, future, itemId)

        return future
    }

    private fun pollQueuedItem(trigger: TriggerContext, action: ActionConfig,
                               apiClient: JenkinsApi, future: CompletableFuture<Int>, itemId: String,
                               startTime: Long = System.currentTimeMillis()) {

        val queuedItem = apiClient.fetchQueuedItem(
                itemId = itemId,
                token = DriverSettings.deployment.apiToken
        ).execute().body()

        queuedItem.executable?.number?.let {
            future.complete(queuedItem.executable.number)

        } ?: run {
            doUnlessTimedOut(trigger, action, startTime, "polling queued item #$itemId") {
                pollQueuedItem(trigger, action, apiClient, future, itemId, startTime)
            }
        }
    }

    override fun fetchExecutionInfo(trigger: TriggerContext, action: ActionConfig, executionId: Int, startTime: Long) {
        val call = actionDriver.buildApiClient().fetchBuild(
                jobName = action.jobId,
                buildId = executionId.toString(),
                token = DriverSettings.deployment.apiToken
        )

        call.enqueue(object : Callback<BuildDetails> {
            override fun onFailure(call: Call<BuildDetails>, cause: Throwable) =
                    handleStatusPollFailure(trigger, action, executionId, cause)

            override fun onResponse(call: Call<BuildDetails>, response: Response<BuildDetails>) {
                if (response.isSuccessful) {
                    val status = mapStatus(response.body())
                    processExecutionInfo(trigger, action, executionId, startTime, status)

                } else {
                    handleStatusPollError(trigger, action, executionId, response.errorBody().string())
                }
            }
        })
    }

    override fun fetchExecutionOutput(trigger: TriggerContext, action: ActionConfig, executionId: Int) {
        logger.warn("Method not implemented yet")
    }
}
