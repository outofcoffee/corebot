package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.chat.model.Action
import com.gatehill.rundeckbot.chat.model.CustomAction
import com.gatehill.rundeckbot.chat.model.SystemAction
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.model.ActionConfig
import java.util.*

private val configService = ConfigService
private val templateService = TemplateService

/**
 * Actions that can be performed. Doubles as a list of named permissions in the security configuration.
 */
enum class ActionType(val description: String) {
    TRIGGER("trigger job"),
    ENABLE("enable job"),
    DISABLE("disable job"),
    LOCK("lock"),
    UNLOCK("unlock"),
    STATUS("check status"),
    HELP("show help")
}

/**
 * An abstract representation of a templated action.
 */
interface ActionTemplate {
    val builtIn: Boolean
    val showInUsage: Boolean
    val actionType: ActionType
    val actionConfigs: List<ActionConfig>
    val tokens: Queue<String>
    val placeholderValues: Map<String, String>

    /**
     * Process the token and return true if it was accepted.
     */
    fun accept(input: String): Boolean

    /**
     * List the actions from this template.
     */
    fun buildActions(): List<Action>
}

/**
 * Parses tokens into placeholder values.
 */
abstract class AbstractActionTemplate : ActionTemplate {
    override val placeholderValues = HashMap<String, String>()

    override fun accept(input: String): Boolean {
        if (tokens.size == 0) return false
        val token = tokens.poll()

        val match = "\\{(.*)\\}".toRegex().matchEntire(token)
        if (null == match) {
            // syntactic sugar
            return (token == input)

        } else {
            // option placeholder
            placeholderValues[match.groupValues[1]] = input
            return true
        }
    }

    /**
     * A short, human readable description.
     */
    protected fun buildShortDescription(actionConfig: ActionConfig? = null): String {
        return if (null != actionConfig) "_${actionType.description}_ on *${actionConfig.name}*" else "_${actionType.description}_"
    }

    /**
     * The response message sent when this actionType is fired.
     */
    protected open fun buildMessage(actionConfig: ActionConfig? = null): String {
        return if (null != actionConfig) "I'm working on *${actionConfig.name}*..." else "I'm working on it..."
    }
}

/**
 * Represents a system action.
 */
abstract class SystemActionTemplate : AbstractActionTemplate() {
    override val builtIn = true
    override val actionConfigs: List<ActionConfig> = emptyList()

    override fun buildActions(): List<Action> {
        return listOf(SystemAction(actionType, buildShortDescription(), buildMessage()))
    }
}

/**
 * Represents a custom action.
 */
abstract class CustomActionTemplate : AbstractActionTemplate() {
    /**
     * List the actions from this template.
     */
    override fun buildActions(): List<Action> {
        return actionConfigs.map { actionConfig ->
            CustomAction(actionType, buildShortDescription(actionConfig), buildMessage(actionConfig),
                    actionConfig, placeholderValues)
        }
    }
}

/**
 * Represents a simple operation for a named action.
 */
abstract class NamedActionTemplate : CustomActionTemplate() {
    protected val actionPlaceholder = "action name"
    override val builtIn: Boolean = true
    override val showInUsage: Boolean = true
    override var actionConfigs: MutableList<ActionConfig> = mutableListOf()

    override fun accept(input: String): Boolean {
        val accepted = super.accept(input)

        // has action been set?
        if (accepted && tokens.isEmpty()) {
            val actionName = placeholderValues[actionPlaceholder]
            val actionConfigs = configService.actions()

            val actionConfig = actionConfigs[actionName]
            if (null != actionConfig) {
                // exact action name match
                this.actionConfigs.add(actionConfig)

            } else {
                // check tags
                actionConfigs.values.forEach { config ->
                    config.tags
                            ?.filter { tag -> tag == actionName || actionName == "all" }
                            ?.forEach { tag -> this.actionConfigs.add(config) }
                }

                return (this.actionConfigs.size > 0)
            }
        }

        return accepted
    }
}

/**
 * Shows a help/usage message.
 */
class ShowHelpTemplate : SystemActionTemplate() {
    override val showInUsage = false
    override val actionType = ActionType.HELP
    override val tokens = LinkedList(listOf("help"))

    override fun buildMessage(actionConfig: ActionConfig?): String {
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
    override var actionConfigs: List<ActionConfig>
    override val tokens: Queue<String>

    constructor(action: ActionConfig) {
        this.actionConfigs = mutableListOf(action)
        tokens = LinkedList(action.template.split("\\s".toRegex()).filterNot(String::isBlank))
    }

    override fun buildMessage(actionConfig: ActionConfig?): String {
        actionConfig ?: throw IllegalArgumentException("Empty actionConfig")

        val msg = StringBuilder()
        msg.append("${ChatLines.pleaseWait()}, I'm running *${actionConfig.name}*")

        if (placeholderValues.size > 0) {
            msg.append(" with these options:")
            placeholderValues.forEach { arg -> msg.append("\r\n- ${arg.key}: _${arg.value}_") }
        } else {
            msg.append(".")
        }

        return msg.toString()
    }
}

class EnableJobTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.ENABLE
    override val tokens = LinkedList(listOf("enable", "{${actionPlaceholder}}"))
}

class DisableJobTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.DISABLE
    override val tokens = LinkedList(listOf("disable", "{${actionPlaceholder}}"))
}

class LockActionTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.LOCK
    override val tokens = LinkedList(listOf("lock", "{${actionPlaceholder}}"))
}

class UnlockActionTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.UNLOCK
    override val tokens = LinkedList(listOf("unlock", "{${actionPlaceholder}}"))
}

class StatusJobTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.STATUS
    override val tokens = LinkedList(listOf("status", "{${actionPlaceholder}}"))
}
