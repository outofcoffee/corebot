package com.gatehill.corebot.config

/**
 * Base class to allow environment to be injected or read from `System`.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class EnvironmentSettings {
    var env: Map<String, String>? = null

    protected fun getenv(name: String): String? =
            env?.let { env!![name] } ?: System.getenv(name)
}
