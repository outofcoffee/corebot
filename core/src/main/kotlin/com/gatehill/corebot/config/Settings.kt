package com.gatehill.corebot.config

import java.io.File

/**
 * System-wide settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object Settings {
    class Chat {
        val authToken by lazy { System.getenv("SLACK_AUTH_TOKEN") ?: throw IllegalStateException("Slack auth token missing") }
        val channelNames by lazy {
            (System.getenv("SLACK_CHANNELS") ?: "corebot").split(",").map(String::trim)
        }
    }

    class Deployment {
        val executionTimeout by lazy { System.getenv("EXECUTION_STATUS_TIMEOUT")?.toInt() ?: 120000 }
    }

    val chat = Chat()
    val deployment = Deployment()
    val configFile by lazy { File(System.getenv("BOT_CONFIG") ?: "/opt/corebot/actions.yml") }
    val configCacheSecs by lazy { System.getenv("CACHE_EXPIRY")?.toLong() ?: 60L }
}
