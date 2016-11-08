package com.gatehill.corebot.chat

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.template.CustomActionTemplate
import com.gatehill.corebot.chat.model.template.NamedActionTemplate
import com.gatehill.corebot.chat.model.template.SystemActionTemplate
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Shows a help/usage message.
 */
class ShowHelpTemplate @Inject constructor(private val templateService: TemplateService) : SystemActionTemplate() {
    override val showInUsage = false
    override val actionType = ActionType.HELP
    override val tokens = java.util.LinkedList(listOf("help"))

    override fun buildMessage(options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${ChatLines.greeting()} :simple_smile: Try one of these:\r\n${templateService.usage()}"
    }
}

/**
 * Triggers job execution.
 */
class TriggerJobTemplate : CustomActionTemplate {
    override val builtIn: Boolean = false
    override val showInUsage: Boolean = true
    override val actionType: ActionType = ActionType.TRIGGER
    override val actionConfigs: List<ActionConfig>
    override val tokens: java.util.Queue<String>

    constructor(action: ActionConfig) {
        this.actionConfigs = mutableListOf(action)
        tokens = java.util.LinkedList(action.template.split("\\s".toRegex()).filterNot(String::isBlank))
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
    override val tokens = java.util.LinkedList(listOf("enable", "{${actionPlaceholder}}"))
}

class DisableJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.DISABLE
    override val tokens = java.util.LinkedList(listOf("disable", "{${actionPlaceholder}}"))
}

class LockActionTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.LOCK
    override val tokens = java.util.LinkedList(listOf("lock", "{${actionPlaceholder}}"))
}

class UnlockActionTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.UNLOCK
    override val tokens = java.util.LinkedList(listOf("unlock", "{${actionPlaceholder}}"))
}

class StatusJobTemplate @Inject constructor(configService: ConfigService) : NamedActionTemplate(configService) {
    override val actionType: ActionType = ActionType.STATUS
    override val tokens = java.util.LinkedList(listOf("status", "{${actionPlaceholder}}"))
}
