package com.gatehill.rundeckbot.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ConfigService {
    /**
     * Top level action settings file wrapper.
     */
    data class ActionConfigWrapper(val version: String?,
                                   val actions: Map<String, ActionConfig>?)

    private val configFileVersion = "1"
    private val objectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    private val settings = Settings()

    fun loadActions(): Map<String, ActionConfig> {
        val actionConfigFile = File(settings.configFile)

        val actionConfig = objectMapper.readValue(actionConfigFile, ActionConfigWrapper::class.java) ?:
                throw IllegalStateException("Action configuration at ${actionConfigFile} was null")

        assert(configFileVersion == actionConfig.version) {
            "Unsupported action settings version: ${actionConfig.version} (expected '${configFileVersion}')"
        }

        val actions = actionConfig.actions ?: throw IllegalStateException("No actions section found in configuration")

        actions.forEach { action ->
            action.value.name = action.key
            action.value.jobId ?: throw IllegalStateException("No job ID found for action '${action.value.name}")
        }

        return actions
    }
}
