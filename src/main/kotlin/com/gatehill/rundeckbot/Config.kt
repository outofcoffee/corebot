package com.gatehill.rundeckbot

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Config {
    class Chat {
        val channelName by lazy { System.getenv("SLACK_CHANNEL_NAME") ?: "azbot-test" }
        val authToken by lazy { System.getenv("SLACK_AUTH_TOKEN") ?: throw IllegalStateException("Slack auth token missing") }
    }
    class Deployment {
        val baseUrl by lazy { System.getenv("RUNDECK_BASE_URL") ?: "http://localhost:4440" }
        val authToken by lazy { System.getenv("RUNDECK_AUTH_TOKEN") ?: throw IllegalStateException("Rundeck auth token missing") }
    }

    val chat = Chat()
    val deployment = Deployment()
}
