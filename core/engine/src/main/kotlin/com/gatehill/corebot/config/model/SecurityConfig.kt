package com.gatehill.corebot.config.model

/**
 * Holds the security configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SecurityConfig(roles: Map<String, SecurityRoleConfig>?,
                     val users: Map<String, SecurityUserConfig>) {

    val roles: Map<String, SecurityRoleConfig>

    init {
        this.roles = roles ?: emptyMap()
    }

    override fun toString(): String {
        return "SecurityConfig(users=$users, roles=$roles)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as SecurityConfig

        if (users != other.users) return false
        if (roles != other.roles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = users.hashCode()
        result = 31 * result + roles.hashCode()
        return result
    }
}
