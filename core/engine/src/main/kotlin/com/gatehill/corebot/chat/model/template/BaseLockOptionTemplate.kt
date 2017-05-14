package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.ChatLines
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Locks or unlocks an option value.
 */
abstract class BaseLockOptionTemplate @Inject constructor(private val configService: ConfigService) : CustomActionTemplate() {
    override val builtIn = true
    override val showInUsage = true
    override val actionMessageMode = ActionMessageMode.GROUP
    override val actionConfigs = mutableListOf<ActionConfig>()

    private val optionName: String
        get() = placeholderValues[optionNamePlaceholder]!!

    private val optionValue: String
        get() = placeholderValues[optionValuePlaceholder]!!

    override fun onTemplateSatisfied(): Boolean {
        configService.actions().values.forEach { potentialConfig ->
            val lockableOptions = potentialConfig.options
                    .filter { option -> option.key.equals(optionName, ignoreCase = true) }
                    .filter { option -> option.value.lockable }
                    .keys

            if (lockableOptions.isNotEmpty()) actionConfigs.add(potentialConfig)
        }

        return actionConfigs.isNotEmpty()
    }

    override fun buildStartMessage(options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${ChatLines.pleaseWait()}, I'm ${if (actionType == CoreActionType.LOCK_OPTION) "locking" else "unlocking"} *$optionName $optionValue*..."
    }

    override fun buildCompleteMessage(): String {
        return "I've ${if (actionType == CoreActionType.LOCK_OPTION) "locked :lock:" else "unlocked :unlock:"} *$optionName $optionValue* for you."
    }

    companion object {
        val optionNamePlaceholder = "option name"
        val optionValuePlaceholder = "option value"
    }
}
