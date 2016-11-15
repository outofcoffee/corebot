package com.gatehill.corebot.chat

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.template.CustomActionTemplate
import com.gatehill.corebot.chat.model.template.NamedActionTemplate
import com.gatehill.corebot.chat.model.template.SystemActionTemplate
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import java.util.*
import javax.inject.Inject

/**
 * Shows a help/usage message.
 */
class ShowHelpTemplate @Inject constructor(private val templateService: TemplateService) : SystemActionTemplate() {
    override val showInUsage = false
    override val actionType = ActionType.HELP
    override val tokens = LinkedList(listOf("help"))

    override fun buildMessage(options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${ChatLines.greeting()} :simple_smile: Try one of these:\r\n${templateService.usage()}"
    }
}

/**
 * Triggers job execution.
 */
class TriggerJobTemplate(action: ActionConfig) : CustomActionTemplate() {
    override val builtIn: Boolean = false
    override val showInUsage: Boolean = true
    override val actionType: ActionType = ActionType.TRIGGER
    override val actionConfigs: List<ActionConfig>
    override val tokens: java.util.Queue<String>

    init {
        this.actionConfigs = mutableListOf(action)
        tokens = LinkedList(action.template.split("\\s".toRegex()).filterNot(String::isBlank))
    }

    override fun buildMessage(options: Map<String, String>, actionConfig: ActionConfig?): String {
        actionConfig ?: throw IllegalArgumentException("Empty actionConfig")

        val msg = StringBuilder()
        msg.append("${ChatLines.pleaseWait()}, I'm running *${actionConfig.name}*")

        if (options.isNotEmpty()) {
            msg.append(" with these options:")
            options.forEach { arg -> msg.append("\r\n- ${arg.key}: _${arg.value}_") }
        } else {
            msg.append(".")
        }

        return msg.toString()
    }
}

class EnableJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.ENABLE
    override val tokens = LinkedList(listOf("enable", "{${actionPlaceholder}}"))
}

class DisableJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.DISABLE
    override val tokens = LinkedList(listOf("disable", "{${actionPlaceholder}}"))
}

class LockActionTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.LOCK_ACTION
    override val tokens = LinkedList(listOf("lock", "{${actionPlaceholder}}"))
}

class UnlockActionTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.UNLOCK_ACTION
    override val tokens = LinkedList(listOf("unlock", "{${actionPlaceholder}}"))
}

class StatusJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.STATUS
    override val tokens = LinkedList(listOf("status", "{${actionPlaceholder}}"))
}

abstract class BaseLockOptionTemplate @Inject constructor(private val configService: ConfigService) : CustomActionTemplate() {
    override val builtIn = true
    override val showInUsage = true
    override val actionConfigs = mutableListOf<ActionConfig>()

    override fun onTemplateSatisfied(): Boolean {
        val optionName = placeholderValues[optionNamePlaceholder]!!

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
        val optionNamePlaceholder = "option name"
        val optionValuePlaceholder = "option value"
    }
}

class LockOptionTemplate @Inject constructor(configService: ConfigService) : BaseLockOptionTemplate(configService) {
    override val actionType: ActionType = ActionType.LOCK_OPTION
    override val tokens = LinkedList(listOf("lock", "{${optionNamePlaceholder}}", "{${optionValuePlaceholder}}"))
}

class UnlockOptionTemplate @Inject constructor(configService: ConfigService) : BaseLockOptionTemplate(configService) {
    override val actionType: ActionType = ActionType.UNLOCK_OPTION
    override val tokens = LinkedList(listOf("unlock", "{${optionNamePlaceholder}}", "{${optionValuePlaceholder}}"))
}
