package com.gatehill.corebot.config

import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.SecurityConfig

/**
 * Provides access to system configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ConfigService {
    /**
     * The message to post when starting up.
     */
    val joinMessage: String?

    /**
     * The action configuration.
     */
    fun actions(): Map<String, ActionConfig>

    /**
     * The security configuration.
     */
    fun security(): SecurityConfig
}
