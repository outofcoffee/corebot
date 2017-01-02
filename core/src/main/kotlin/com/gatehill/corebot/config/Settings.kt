package com.gatehill.corebot.config

import org.apache.logging.log4j.LogManager
import java.io.File

/**
 * System-wide settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object Settings {
    private val logger = LogManager.getLogger(Settings::class.java)!!

    class Chat {
        val authToken by lazy { System.getenv("SLACK_AUTH_TOKEN") ?: throw IllegalStateException("Slack auth token missing") }
        val channelNames by lazy {
            (System.getenv("SLACK_CHANNELS") ?: "corebot").split(",").map(String::trim)
        }
    }

    class Deployment {
        val executionTimeout by lazy { (System.getenv("EXECUTION_STATUS_TIMEOUT")?.toInt() ?: 120) * 1000 }
    }

    val chat = Chat()
    val deployment = Deployment()

    /**
     * The file containing the action configuration.
     */
    val actionConfigFile by lazy {
        val configFile: String? = System.getenv("BOT_CONFIG")?.apply {
            logger.warn("Variable 'BOT_CONFIG' is deprecated and will be removed in a future release - use 'ACTION_CONFIG_FILE' or 'ACTION_CONFIG' instead")
        } ?: System.getenv("ACTION_CONFIG_FILE")

        File(configFile ?: "/opt/corebot/actions.yml")
    }
    val configCacheSecs by lazy { System.getenv("CACHE_EXPIRY")?.toLong() ?: 60L }
}
