package com.gatehill.corebot.config

import com.gatehill.corebot.classloader.ClassLoaderUtil
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.DeserialisedActionConfig
import com.gatehill.corebot.config.model.OptionConfig
import com.gatehill.corebot.config.model.SecurityConfig
import com.gatehill.corebot.config.model.SecurityUserConfig
import com.gatehill.corebot.config.model.SystemConfig
import com.gatehill.corebot.util.yamlMapper
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream
import java.util.HashMap
import java.util.concurrent.TimeUnit

/**
 * Provides access to system configuration.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class ConfigServiceImpl : ConfigService {
    private interface VersionedConfig {
        val version: String
    }

    protected abstract class VersionedSecurityConfig(override val version: String,
                                                     open val security: SecurityConfig?) : VersionedConfig

    /**
     * The system configuration file wrapper.
     */
    private data class SystemConfigWrapper(override val version: String,
                                           val system: SystemConfig) : VersionedConfig

    /**
     * The security configuration file wrapper.
     */
    private data class SecurityConfigWrapper(override val version: String,
                                             override val security: SecurityConfig) : VersionedSecurityConfig(version, security)

    /**
     * Top level action settings file wrapper.
     */
    protected class ActionConfigWrapper(override val version: String,
                                        val joinMessage: String?,
                                        security: SecurityConfig?,
                                        val actions: Map<String, DeserialisedActionConfig>) : VersionedSecurityConfig(version, security)

    private companion object {
        const val configFileVersion = "1"
    }

    private val logger: Logger = LogManager.getLogger(ConfigServiceImpl::class.java)

    /**
     * Immutable default security configuration.
     */
    private val defaultSecurity by lazy {
        loadFile(ClassLoaderUtil.classLoader.getResourceAsStream("default-security.yml"),
                SecurityConfigWrapper::class.java).security
    }

    /**
     * Caches configuration.
     */
    private val configCache: Cache<String, Any> = CacheBuilder.newBuilder()
            .expireAfterWrite(Settings.configCacheSecs, TimeUnit.SECONDS)
            .removalListener<String, Any>({ logger.debug("Cached configuration '${it.key}' expired") })
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
     * The system configuration.
     */
    @Suppress("UNCHECKED_CAST")
    override fun system(): SystemConfig =
            configCache.get("system") { loadSystemConfig() } as SystemConfig

    /**
     * Loads the action configuration from file.
     */
    private fun loadActionConfig(): Map<String, ActionConfig> {
        val config = loadCustomConfig()
        logger.debug("Loaded ${config.actions.size} operations")

        val actions = mutableMapOf<String, ActionConfig>()
        val systemDefaults = system().defaults

        config.actions.map { action ->
            with(action.value) {
                // all action operations have the 'all' tag
                val combinedTags = mutableListOf("all")
                combinedTags.addAll(tags)

                // action configuration options should override defaults
                val allOptions = mutableMapOf<String, OptionConfig>().apply {
                    putAll(systemDefaults.options)
                    putAll(options)
                }

                actions[action.key] = ActionConfig(
                        template = template,
                        jobId = jobId,
                        name = action.key,
                        options = allOptions,
                        tags = combinedTags,
                        driver = action.value.driver ?: systemDefaults.driver,
                        showJobOutput = action.value.showJobOutput ?: systemDefaults.showJobOutput,
                        showJobOutcome = action.value.showJobOutcome ?: systemDefaults.showJobOutcome,
                        runAsTriggerUser = action.value.runAsTriggerUser ?: systemDefaults.runAsTriggerUser
                )
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
            allUsers.putAll(config.security.users)
        } else {
            allUsers.putAll(defaultSecurity.users)
        }

        security.roles.forEach { role -> role.value.name = role.key }
        security.users.forEach { user -> user.value.name = user.key }

        logger.debug("Loaded ${security.roles.size} security roles and ${security.users.size} users")

        return security
    }

    /**
     * Loads the system configuration from file.
     */
    private fun loadSystemConfig(): SystemConfig {
        val systemConfigFile = Settings.systemConfigFile

        return if (systemConfigFile.exists()) {
            val wrapper = loadFile(systemConfigFile.inputStream(), SystemConfigWrapper::class.java)
            logger.debug("Loaded system configuration from: $systemConfigFile")
            wrapper.system

        } else {
            SystemConfig().apply {
                logger.debug("No system configuration found at: $systemConfigFile - using defaults")
            }
        }
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
            val config = yamlMapper.readValue(configFile, clazz) ?:
                    throw IllegalStateException("Configuration in $configFile was null")

            assert(configFileVersion == config.version) {
                "Unsupported configuration version: ${config.version} (expected '$configFileVersion')"
            }

            return config
        }
    }
}
