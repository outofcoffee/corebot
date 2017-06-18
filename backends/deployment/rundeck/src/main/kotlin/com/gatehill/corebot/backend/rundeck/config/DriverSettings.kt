package com.gatehill.corebot.backend.rundeck.config

/**
 * Driver-specific settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object DriverSettings {
    class Deployment {
        val apiToken by lazy { System.getenv("RUNDECK_API_TOKEN") ?: throw IllegalStateException("Rundeck API token missing") }
        val baseUrl by lazy { System.getenv("RUNDECK_BASE_URL") ?: "http://localhost:4440" }
    }

    val deployment = Deployment()
}
