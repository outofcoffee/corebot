package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Represents a simple operation for a named action.
 */
abstract class NamedActionTemplate @Inject constructor(private val configService: ConfigService) : CustomActionTemplate() {
    protected val actionPlaceholder = "action name"
    override val builtIn: Boolean = true
    override val showInUsage: Boolean = true
    override val actionConfigs: MutableList<ActionConfig> = mutableListOf()

    override fun accept(input: String): Boolean {
        val accepted = super.accept(input)

        // has action been set?
        if (accepted && tokens.isEmpty()) {
            val actionOrTagName = placeholderValues[actionPlaceholder]
            val potentialConfigs = configService.actions()

            val actionConfig = potentialConfigs[actionOrTagName]
            if (null != actionConfig) {
                // exact action name match
                this.actionConfigs.add(actionConfig)

            } else {
                // check tags
                potentialConfigs.values.forEach { potentialConfig ->
                    potentialConfig.tags
                            .filter { tag -> tag == actionOrTagName }
                            .forEach { tag -> this.actionConfigs.add(potentialConfig) }
                }

                return (this.actionConfigs.isNotEmpty())
            }
        }

        return accepted
    }
}
