package com.gatehill.corebot.backend.rundeck.config

import com.gatehill.corebot.config.EnvironmentSettings

/**
 * Driver-specific settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object DriverSettings : EnvironmentSettings() {
    class Deployment {
        val apiToken by lazy { getenv("RUNDECK_API_TOKEN") ?: throw IllegalStateException("Rundeck API token missing") }
        val baseUrl by lazy { getenv("RUNDECK_BASE_URL") ?: "http://localhost:4440" }
    }

    val deployment = Deployment()
}
