package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Common operations for lockable options.
 */
abstract class BaseLockableOptionFactory @Inject constructor(private val configService: ConfigService,
                                                             protected val lockService: LockService) : PlainOperationFactory() {

    protected val optionName: String
        get() = placeholderValues[optionNamePlaceholder]!!

    protected val optionValue: String
        get() = placeholderValues[optionValuePlaceholder]!!

    override fun onSatisfied(): Boolean {
        val actionConfigs = mutableListOf<ActionConfig>()

        configService.actions().values.forEach { potentialConfig ->
            val lockableOptions = potentialConfig.options
                    .filter { option -> option.key.equals(optionName, ignoreCase = true) }
                    .filter { option -> option.value.lockable }
                    .keys

            if (lockableOptions.isNotEmpty()) actionConfigs.add(potentialConfig)
        }

        return actionConfigs.isNotEmpty()
    }

    companion object {
        const val optionNamePlaceholder = "option name"
        const val optionValuePlaceholder = "option value"
    }
}
