package com.gatehill.corebot.driver.jenkins.action

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.driver.ActionDriver
import com.gatehill.corebot.action.driver.JobBaseActionDriver
import com.gatehill.corebot.driver.base.action.ApiClientBuilder
import com.gatehill.corebot.driver.jenkins.config.DriverSettings
import okhttp3.Credentials
import java.util.HashMap
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface JenkinsActionDriver : ActionDriver, ApiClientBuilder<JenkinsApi>

class JenkinsActionDriverImpl @Inject constructor(triggerJobService: JenkinsJobTriggerService,
                                                  lockService: LockService) : JobBaseActionDriver(triggerJobService, lockService), JenkinsActionDriver {
    override val baseUrl: String
        get() = DriverSettings.deployment.baseUrl

    override fun buildApiClient(headers: Map<String, String>): JenkinsApi {
        val allHeaders: MutableMap<String, String> = HashMap(headers)

        // add HTTP Basic Authorization header if configuration is set
        DriverSettings.deployment.username?.let {
            allHeaders.put("Authorization", Credentials.basic(it, DriverSettings.deployment.password))
        }

        return buildApiClient(JenkinsApi::class.java, allHeaders)
    }
}
