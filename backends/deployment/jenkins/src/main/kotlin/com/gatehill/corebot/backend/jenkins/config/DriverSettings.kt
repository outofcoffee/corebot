package com.gatehill.corebot.backend.jenkins.config

/**
 * Driver-specific settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object DriverSettings {
    class Deployment {
        val apiToken: String? by lazy { System.getenv("JENKINS_API_TOKEN") }
        val baseUrl by lazy { System.getenv("JENKINS_BASE_URL") ?: "http://localhost:8080" }
        val username: String? by lazy { System.getenv("JENKINS_USERNAME") }
        val password: String? by lazy { System.getenv("JENKINS_PASSWORD") }
    }

    val deployment = Deployment()
}
