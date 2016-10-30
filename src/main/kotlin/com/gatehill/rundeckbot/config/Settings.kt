package com.gatehill.rundeckbot.config

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object Settings {
    class Chat {
        val authToken by lazy { System.getenv("SLACK_AUTH_TOKEN") ?: throw IllegalStateException("Slack auth token missing") }
        val channelName by lazy { System.getenv("SLACK_CHANNEL_NAME") ?: "rundeck-slackbot" }
    }

    class Deployment {
        val apiToken by lazy { System.getenv("RUNDECK_API_TOKEN") ?: throw IllegalStateException("Rundeck API token missing") }
        val baseUrl by lazy { System.getenv("RUNDECK_BASE_URL") ?: "http://localhost:4440" }
        val executionTimeout by lazy { System.getenv("RUNDECK_EXECUTION_TIMEOUT")?.toInt() ?: 12000 }
    }

    val chat = Chat()
    val deployment = Deployment()
    val configFile by lazy { System.getenv("BOT_CONFIG") ?: "/opt/rundeck-slackbot/actions.yml" }
}
