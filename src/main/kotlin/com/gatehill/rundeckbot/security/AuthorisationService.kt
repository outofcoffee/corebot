package com.gatehill.rundeckbot.security

import com.gatehill.rundeckbot.chat.ChatService
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.SecurityConfig
import com.gatehill.rundeckbot.config.SecurityUserConfig
import org.apache.logging.log4j.LogManager

/**
 * Performs access control checks.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object AuthorisationService {
    private val logger = LogManager.getLogger(AuthorisationService::class.java)!!
    private val configService by lazy { ConfigService }

    fun checkPermission(action: ChatService.Action, callback: (Boolean) -> Unit, userName: String?) {
        val security = configService.loadSecurity()

        // check for the username explicitly as well as the all users wildcard
        val permitted = checkUserPermissions(action, security, security.users[userName]) ||
                (checkUserPermissions(action, security, security.users["*"]))

        logger.debug("User '{}' action {}: {}", userName, action.actionType.name,
                if (permitted) "PERMITTED" else "DENIED")

        callback(permitted)
    }

    private fun checkUserPermissions(action: ChatService.Action, security: SecurityConfig,
                                     user: SecurityUserConfig?): Boolean {

        return (user?.roles ?: emptyList()).any { userRole ->
            // check the security config for the user's role
            val roleConfig = security.roles[userRole]
            if (null != roleConfig) {
                val matchedPermissions = roleConfig.permissions.filter { permission ->
                    action.actionType.name.equals(permission, ignoreCase = true)
                }

                // only permitted if permission is present in user's role
                if (matchedPermissions.any()) return@any true
            }

            // fail safe
            return@any false
        }
    }
}
