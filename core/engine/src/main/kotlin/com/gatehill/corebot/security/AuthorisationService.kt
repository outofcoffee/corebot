package com.gatehill.corebot.security

import com.gatehill.corebot.operation.model.Operation
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.SecurityConfig
import com.gatehill.corebot.config.model.SecurityUserConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Performs access control checks.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class AuthorisationService @Inject constructor(private val configService: ConfigService) {
    private val logger: Logger = LogManager.getLogger(AuthorisationService::class.java)

    fun checkPermission(operation: Operation, callback: (Boolean) -> Unit, userName: String?) {
        val security = configService.security()

        // check for the username explicitly as well as the all users wildcard
        val permitted = checkUserPermissions(operation, security, security.users[userName]) ||
                (checkUserPermissions(operation, security, security.users["*"]))

        logger.debug("User '{}' operation {}: {}", userName, operation.operationType.name,
                if (permitted) "PERMITTED" else "DENIED")

        callback(permitted)
    }

    private fun checkUserPermissions(operation: Operation, security: SecurityConfig,
                                     user: SecurityUserConfig?): Boolean {

        return (user?.roles ?: emptyList()).any { userRole ->
            // check the security config for the user's role
            val roleConfig = security.roles[userRole]
            roleConfig?.let {
                val matchedPermissions = roleConfig.permissions.filter { permission ->
                    operation.operationType.name.equals(permission, ignoreCase = true)
                }.any()

                // no tags on a role means it applies to all
                val matchedTags = (null == roleConfig.tags ||
                        roleConfig.tags.isEmpty() ||
                        roleConfig.tags.intersect(operation.tags).any())

                // only permitted if permission is present in user's role and tags match
                if (matchedPermissions && matchedTags) return@any true
            }

            // fail safe
            return@any false
        }
    }
}
