package com.gatehill.corebot.frontend.http.config

/**
 * Chat settings.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ChatSettings {
    val hostname by lazy { System.getenv("HTTP_BIND_HOST") ?: "0.0.0.0" }
    val port by lazy { System.getenv("HTTP_BIND_PORT")?.toInt() ?: 8080 }
}
