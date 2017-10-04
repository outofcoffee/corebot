package com.gatehill.corebot.config.model

/**
 * Holds the system configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class SystemConfig(val defaults: DefaultsConfig = DefaultsConfig(),
                        val requestHeaders: Map<String, String> = emptyMap()) {

    class DefaultsConfig(val driver: String = defaultDriver,
                         val showJobOutput: Boolean = false,
                         val showJobOutcome: Boolean = true,
                         val runAsTriggerUser: Boolean = false,
                         val options: Map<String, OptionConfig> = emptyMap()) {

        companion object {
            const val defaultDriver = "rundeck"
        }
    }
}
