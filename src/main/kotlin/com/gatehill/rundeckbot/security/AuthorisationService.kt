package com.gatehill.rundeckbot.security

import com.gatehill.rundeckbot.chat.model.Action
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.model.SecurityConfig
import com.gatehill.rundeckbot.config.model.SecurityUserConfig
import org.apache.logging.log4j.LogManager

/**
 * Performs access control checks.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object AuthorisationService {
    private val logger = LogManager.getLogger(AuthorisationService::class.java)!!
    private val configService by lazy { ConfigService }

    fun checkPermission(action: Action, callback: (Boolean) -> Unit, userName: String?) {
        val security = configService.security()

        // check for the username explicitly as well as the all users wildcard
        val permitted = checkUserPermissions(action, security, security.users[userName]) ||
                (checkUserPermissions(action, security, security.users["*"]))

        logger.debug("User '{}' action {}: {}", userName, action.actionType.name,
                if (permitted) "PERMITTED" else "DENIED")

        callback(permitted)
    }

    private fun checkUserPermissions(action: Action, security: SecurityConfig,
                                     user: SecurityUserConfig?): Boolean {

        return (user?.roles ?: emptyList()).any { userRole ->
            // check the security config for the user's role
            val roleConfig = security.roles[userRole]
            roleConfig?.let {
                val matchedPermissions = roleConfig.permissions.filter { permission ->
                    action.actionType.name.equals(permission, ignoreCase = true)
                }.any()

                // no tags on a role means it applies to all
                val matchedTags = (null == roleConfig.tags ||
                        roleConfig.tags.isEmpty() ||
                        roleConfig.tags.intersect(action.tags).any())

                // only permitted if permission is present in user's role and tags match
                if (matchedPermissions && matchedTags) return@any true
            }

            // fail safe
            return@any false
        }
    }
}
