package com.gatehill.corebot.config

/**
 * Chat settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ChatSettings {
    val hostname by lazy { System.getenv("WEBSOCKET_BIND_HOSTNAME") ?: "localhost" }
    val port by lazy { System.getenv("WEBSOCKET_BIND_PORT")?.toInt() ?: 8025 }

    val echoEventsToAllSessions by lazy { System.getenv("WEBSOCKET_ECHO_EVENTS_TO_ALL")?.toBoolean() ?: false }
}
