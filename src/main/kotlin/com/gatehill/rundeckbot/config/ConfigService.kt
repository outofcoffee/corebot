package com.gatehill.rundeckbot.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.*

/**
 * Provides access to system configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
object ConfigService {
    private interface VersionedConfig {
        val version: String
        val security: SecurityConfig?
    }

    /**
     * The security configuration file wrapper.
     */
    private data class SecurityConfigWrapper(override val version: String,
                                             override val security: SecurityConfig) : VersionedConfig

    /**
     * Top level action settings file wrapper.
     */
    private data class ActionConfigWrapper(override val version: String,
                                           override val security: SecurityConfig?,
                                           val actions: Map<String, ActionConfig>) : VersionedConfig

    private val configFileVersion = "1"
    private val objectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    private val logger = LogManager.getLogger(ConfigService::class.java)!!
    private val defaultSecurityConfigFile by lazy { File(this.javaClass.getResource("/default-security.yml").toURI()) }

    fun loadActions(): Map<String, ActionConfig> {
        val config = loadConfig()

        config.actions.forEach { action -> action.value.name = action.key }

        logger.debug("Loaded ${config.actions.size} actions")

        return config.actions
    }

    fun loadSecurity(): SecurityConfig {
        val defaultSecurity = loadFile(defaultSecurityConfigFile, SecurityConfigWrapper::class.java).security

        val allRoles = HashMap(defaultSecurity.roles)
        val allUsers : MutableMap<String, SecurityUserConfig> = HashMap()
        val security = SecurityConfig(allRoles, allUsers)

        val config = loadConfig()
        config.security ?: logger.warn("No user security configuration found - using defaults")

        if (null != config.security?.roles) {
            // config roles override or add if absent
            allRoles.putAll(config.security!!.roles)
        }

        if (null != config.security?.users && config.security?.users.size > 0) {
            // users defined in config always override defaults
            allUsers.putAll(config.security!!.users)
        } else {
            allUsers.putAll(defaultSecurity.users)
        }

        security.roles.forEach { role -> role.value.name = role.key }
        security.users.forEach { user -> user.value.name = user.key }

        logger.debug("Loaded ${security.roles.size} security roles and ${security.users.size} users")

        return security
    }

    private fun loadConfig() = loadFile(Settings.configFile, ActionConfigWrapper::class.java)

    private fun <T : VersionedConfig> loadFile(configFile: File, clazz: Class<T>): T {
        val config = objectMapper.readValue(configFile, clazz) ?:
                throw IllegalStateException("Configuration in ${configFile} was null")

        assert(configFileVersion == config.version) {
            "Unsupported configuration version: ${config.version} (expected '${configFileVersion}')"
        }

        return config
    }
}
