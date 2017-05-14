package com.gatehill.corebot.config.model

/**
 * Models a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class SecurityUserConfig(val roles: List<String>) {
    lateinit var name: String
}
