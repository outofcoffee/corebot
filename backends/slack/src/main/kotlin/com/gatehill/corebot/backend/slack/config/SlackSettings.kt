package com.gatehill.corebot.backend.slack.config

/**
 * Slack driver settings.
 */
object SlackSettings {
    val slackUserToken by lazy { System.getenv("SLACK_USER_TOKEN") ?: throw IllegalStateException("Slack user token missing") }

    val members: List<String> by lazy {
        System.getenv("SLACK_CHANNEL_MEMBERS")?.let { members ->
            members.split(",").map(String::trim)

        } ?: throw IllegalStateException("Slack channel members list missing")
    }
}
