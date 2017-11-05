package com.gatehill.corebot.backend.rundeck.action

import com.gatehill.corebot.backend.rundeck.config.DriverSettings
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.driver.base.action.ApiClientBuilder
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class RundeckApiClientBuilder @Inject constructor(private val configService: ConfigService) : ApiClientBuilder<RundeckApi> {
    override val baseUrl: String
        get() = DriverSettings.deployment.baseUrl

    override fun buildApiClient(headers: Map<String, String>): RundeckApi {
        val allHeaders: Map<String, String> = headers.toMutableMap().apply {
            putAll(configService.system().requestHeaders)
            put("X-Rundeck-Auth-Token", DriverSettings.deployment.apiToken)
        }
        return buildApiClient(RundeckApi::class.java, allHeaders)
    }
}
