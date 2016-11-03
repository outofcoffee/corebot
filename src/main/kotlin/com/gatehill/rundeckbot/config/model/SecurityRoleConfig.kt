package com.gatehill.rundeckbot.config.model

/**
 * Models a security role.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class SecurityRoleConfig(val permissions: List<String>,
                              val tags: List<String>) {

    lateinit var name: String
}
