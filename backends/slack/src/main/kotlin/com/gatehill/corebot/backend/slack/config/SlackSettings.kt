package com.gatehill.corebot.backend.slack.config

/**
 * Slack driver settings.
 */
object SlackSettings {
    val authToken by lazy { System.getenv("SLACK_AUTH_TOKEN") ?: throw IllegalStateException("Slack auth token missing") }

    val members : List<String> by lazy { listOf("pcornish") }
}
