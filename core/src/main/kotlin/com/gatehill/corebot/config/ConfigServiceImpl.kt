package com.gatehill.corebot.config

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.SecurityConfig
import com.gatehill.corebot.config.model.SecurityUserConfig
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Provides access to system configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class ConfigServiceImpl : ConfigService {
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
    protected class ActionConfigWrapper(override val version: String,
                                        val joinMessage: String?,
                                        override val security: SecurityConfig?,
                                        val actions: Map<String, ActionConfig>) : VersionedConfig

    private val configFileVersion = "1"
    private val logger: Logger = LogManager.getLogger(ConfigServiceImpl::class.java)
    private val objectMapper = YAMLMapper().registerKotlinModule()

    /**
     * Immutable default security configuration.
     */
    private val defaultSecurity by lazy {
        loadFile(this.javaClass.getResourceAsStream("/default-security.yml"),
                SecurityConfigWrapper::class.java).security
    }

    /**
     * Caches configuration.
     */
    private val configCache: Cache<String, Any> = CacheBuilder.newBuilder()
            .expireAfterWrite(Settings.configCacheSecs, TimeUnit.SECONDS)
            .removalListener({ logger.debug("Cached configuration '${it.key}' expired") })
            .build<String, Any>()

    /**
     * The action configuration.
     */
    @Suppress("UNCHECKED_CAST")
    override fun actions(): Map<String, ActionConfig> =
            configCache.get("actions") { loadActionConfig() } as Map<String, ActionConfig>

    /**
     * The security configuration.
     */
    @Suppress("UNCHECKED_CAST")
    override fun security(): SecurityConfig =
            configCache.get("security") { loadSecurityConfig() } as SecurityConfig

    /**
     * Loads the action configuration from file.
     */
    private fun loadActionConfig(): Map<String, ActionConfig> {
        val config = loadCustomConfig()
        logger.debug("Loaded ${config.actions.size} actions")

        val actions = mutableMapOf<String, ActionConfig>()
        config.actions.map { action ->
            with(action.value) {
                // all custom actions have the 'all' tag
                val combinedTags = mutableListOf("all")
                combinedTags.addAll(tags)

                actions[action.key] = ActionConfig(template,
                        jobId,
                        action.key,
                        options,
                        combinedTags,
                        action.value.driver,
                        action.value.showJobOutput,
                        action.value.showJobOutcome,
                        action.value.runAsTriggerUser)
            }
        }
        return actions
    }

    /**
     * The message to post when starting up.
     */
    override val joinMessage: String?
        get() = loadCustomConfig().joinMessage

    /**
     * Loads the security configuration from file.
     */
    private fun loadSecurityConfig(): SecurityConfig {
        val allRoles = HashMap(defaultSecurity.roles)
        val allUsers = mutableMapOf<String, SecurityUserConfig>()
        val security = SecurityConfig(allRoles, allUsers)

        val config = loadCustomConfig()
        config.security ?: logger.warn("No custom user security configuration found - using defaults")

        config.security?.roles?.let {
            // config roles override or add if absent
            allRoles.putAll(config.security.roles)
        }

        if (config.security?.users?.isNotEmpty() == true) {
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

    /**
     * Load the custom configuration.
     *
     * Allow subclasses to override this behaviour.
     */
    protected open fun loadCustomConfig() =
            loadFile(Settings.actionConfigFile.inputStream(), ActionConfigWrapper::class.java)

    /**
     * Load a versioned configuration from file.
     */
    protected fun <T : VersionedConfig> loadFile(configFile: InputStream, clazz: Class<T>): T {
        configFile.use {
            val config = objectMapper.readValue(configFile, clazz) ?:
                    throw IllegalStateException("Configuration in ${configFile} was null")

            assert(configFileVersion == config.version) {
                "Unsupported configuration version: ${config.version} (expected '${configFileVersion}')"
            }

            return config
        }
    }
}
