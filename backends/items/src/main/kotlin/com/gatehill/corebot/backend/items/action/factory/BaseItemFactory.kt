package com.gatehill.corebot.backend.items.action.factory

import com.gatehill.corebot.operation.factory.ActionOperationFactory
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Common item functionality.
 */
abstract class BaseItemFactory @Inject constructor(private val configService: ConfigService) : ActionOperationFactory() {
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
