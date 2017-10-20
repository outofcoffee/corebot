package com.gatehill.corebot.frontend.slack.config

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
        val postJoinMessage by lazy { System.getenv("SLACK_ENABLE_JOIN_MESSAGE")?.toBoolean() ?: true }

        /**
         * The time in seconds to cache message IDs for de-duplication purposes.
         */
        val messageIdCache by lazy { System.getenv("SLACK_MESSAGE_ID_CACHE")?.toLong() ?: 120 }
    }

    class Threads {
        val replyInThread by lazy { System.getenv("SLACK_REPLY_IN_THREAD")?.toBoolean() ?: false }
        val allowThreadedTriggers by lazy { System.getenv("SLACK_ALLOW_THREADED_TRIGGERS")?.toBoolean() ?: false }
    }

    val chat = Chat()
    val threads = Threads()
}
