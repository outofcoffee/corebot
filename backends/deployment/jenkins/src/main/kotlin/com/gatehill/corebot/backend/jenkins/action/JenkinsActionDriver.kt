package com.gatehill.corebot.backend.jenkins.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.driver.ActionDriver
import com.gatehill.corebot.driver.base.action.ApiClientBuilder
import com.gatehill.corebot.backend.jenkins.config.DriverSettings
import com.gatehill.corebot.backend.jobs.action.JobBaseActionDriver
import okhttp3.Credentials
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface JenkinsActionDriver : ActionDriver, ApiClientBuilder<JenkinsApi>

class JenkinsActionDriverImpl @Inject constructor(triggerJobService: JenkinsJobTriggerService,
                                                  lockService: LockService,
                                                  private val configService: ConfigService) : JobBaseActionDriver(triggerJobService, lockService), JenkinsActionDriver {
    override val baseUrl: String
        get() = DriverSettings.deployment.baseUrl

    override fun buildApiClient(headers: Map<String, String>): JenkinsApi {
        val allHeaders: Map<String, String> = headers.toMutableMap().apply {
            putAll(configService.system().requestHeaders)

            // add HTTP Basic Authorization header if configuration is set
            DriverSettings.deployment.username?.let {
                put("Authorization", Credentials.basic(it, DriverSettings.deployment.password))
            }
        }

        return buildApiClient(JenkinsApi::class.java, allHeaders)
    }
}
