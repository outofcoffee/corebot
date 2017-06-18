package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Represents a simple operation for a named action.
 */
abstract class NamedActionFactory @Inject constructor(private val configService: ConfigService) : ActionOperationFactory() {
    override val actionConfigs = mutableListOf<ActionConfig>()

    override fun onSatisfied(): Boolean {
        val actionOrTagName = placeholderValues[actionPlaceholder]
        val potentialConfigs = configService.actions()

        potentialConfigs[actionOrTagName]?.let { actionConfig ->
            // exact action name match
            actionConfigs.add(actionConfig)

        } ?: run {
            // check tags
            potentialConfigs.values.forEach { potentialConfig ->
                if (potentialConfig.tags.any { tag -> tag == actionOrTagName })
                    actionConfigs.add(potentialConfig)
            }
        }

        return actionConfigs.isNotEmpty()
    }

    companion object {
        const val actionPlaceholder = "action or tag name"
    }
}
