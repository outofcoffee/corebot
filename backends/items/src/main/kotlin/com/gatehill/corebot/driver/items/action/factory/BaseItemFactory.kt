package com.gatehill.corebot.driver.items.action.factory

import com.gatehill.corebot.action.factory.ActionMessageMode
import com.gatehill.corebot.action.factory.CustomActionFactory
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Common item functionality.
 */
abstract class BaseItemFactory @Inject constructor(private val configService: ConfigService) : CustomActionFactory() {
    override val builtIn = false
    override val showInUsage = true
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
    override val actionConfigs = mutableListOf<ActionConfig>()

    protected val itemName: String
        get() = placeholderValues[itemPlaceholder]!!

    override fun onSatisfied(): Boolean {
        actionConfigs += configService.actions()
                .filterKeys { it.equals(itemName, ignoreCase = true) }
                .values

        return actionConfigs.isNotEmpty()
    }

    companion object {
        const val itemPlaceholder = "itemName"
    }
}
