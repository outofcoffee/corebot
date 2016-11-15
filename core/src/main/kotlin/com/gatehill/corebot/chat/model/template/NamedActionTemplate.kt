package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Represents a simple operation for a named action.
 */
abstract class NamedActionTemplate @Inject constructor(private val configService: ConfigService) : CustomActionTemplate() {
    protected val actionPlaceholder = "action or tag name"
    override val builtIn: Boolean = true
    override val showInUsage: Boolean = true
    override val actionConfigs = mutableListOf<ActionConfig>()

    override fun onTemplateSatisfied(): Boolean {
        val actionOrTagName = placeholderValues[actionPlaceholder]
        val potentialConfigs = configService.actions()

        val actionConfig = potentialConfigs[actionOrTagName]
        if (null != actionConfig) {
            // exact action name match
            actionConfigs.add(actionConfig)

        } else {
            // check tags
            potentialConfigs.values.forEach { potentialConfig ->
                potentialConfig.tags
                        .filter { tag -> tag == actionOrTagName }
                        .forEach { tag -> actionConfigs.add(potentialConfig) }
            }
        }

        return actionConfigs.isNotEmpty()
    }
}
