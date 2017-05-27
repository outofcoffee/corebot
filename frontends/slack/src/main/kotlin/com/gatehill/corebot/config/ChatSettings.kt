package com.gatehill.corebot.config

/**
 * Chat settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ChatSettings {
    class Chat {
        val authToken by lazy { System.getenv("SLACK_AUTH_TOKEN") ?: throw IllegalStateException("Slack auth token missing") }
        val channelNames by lazy {
            (System.getenv("SLACK_CHANNELS") ?: "corebot").split(",").map(String::trim)
        }
    }

    val chat = Chat()
}
