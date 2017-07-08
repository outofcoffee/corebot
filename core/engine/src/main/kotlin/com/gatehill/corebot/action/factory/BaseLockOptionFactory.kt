package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.CoreActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Locks or unlocks an option value.
 */
abstract class BaseLockOptionFactory @Inject constructor(private val configService: ConfigService,
                                                         private val chatGenerator: ChatGenerator) : CustomActionFactory() {

    override val actionConfigs = mutableListOf<ActionConfig>()

    private val optionName: String
        get() = placeholderValues[optionNamePlaceholder]!!

    private val optionValue: String
        get() = placeholderValues[optionValuePlaceholder]!!

    override fun onSatisfied(): Boolean {
        configService.actions().values.forEach { potentialConfig ->
            val lockableOptions = potentialConfig.options
                    .filter { option -> option.key.equals(optionName, ignoreCase = true) }
                    .filter { option -> option.value.lockable }
                    .keys

            if (lockableOptions.isNotEmpty()) actionConfigs.add(potentialConfig)
        }

        return actionConfigs.isNotEmpty()
    }

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${chatGenerator.pleaseWait()}, I'm ${if (actionType == CoreActionType.LOCK_OPTION) "locking" else "unlocking"} *$optionName $optionValue*..."
    }

    override fun buildCompleteMessage(): String {
        return "I've ${if (actionType == CoreActionType.LOCK_OPTION) "locked :lock:" else "unlocked :unlock:"} *$optionName $optionValue* for you."
    }

    companion object {
        const val optionNamePlaceholder = "option name"
        const val optionValuePlaceholder = "option value"
    }
}
