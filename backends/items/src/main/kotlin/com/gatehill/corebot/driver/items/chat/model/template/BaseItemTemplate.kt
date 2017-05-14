package com.gatehill.corebot.driver.items.chat.model.template

import com.gatehill.corebot.chat.model.template.ActionMessageMode
import com.gatehill.corebot.chat.model.template.CustomActionTemplate
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Common item functionality.
 */
abstract class BaseItemTemplate @Inject constructor(private val configService: ConfigService) : CustomActionTemplate() {
    override val builtIn = false
    override val showInUsage = true
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
    override val actionConfigs = mutableListOf<ActionConfig>()

    protected val itemName: String
        get() = placeholderValues[itemPlaceholder]!!

    override fun onTemplateSatisfied(): Boolean {
        actionConfigs += configService.actions()
                .filterKeys { it.equals(itemName, ignoreCase = true) }
                .values

        return actionConfigs.isNotEmpty()
    }

    companion object {
        val itemPlaceholder = "item name"
    }
}
