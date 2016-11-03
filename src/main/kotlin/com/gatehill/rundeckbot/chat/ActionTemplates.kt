package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.config.ActionConfig
import com.gatehill.rundeckbot.config.ConfigService

/**
 * Actions that can be performed. Doubles as a list of named permissions in the security configuration.
 */
enum class ActionType(val description: String) {
    TRIGGER("trigger job"),
    ENABLE("enable job"),
    DISABLE("disable job"),
    LOCK("lock"),
    UNLOCK("unlock"),
    STATUS("check status")
}

/**
 * Convenience method to read an attribute from an ActionConfig.
 */
fun readActionAttribute(actions: List<ActionConfig>, supplier: (ActionConfig) -> String): String {
    val names = StringBuilder()

    actions.forEach { action ->
        if (names.length > 0) names.append(", ")
        names.append(supplier(action))
    }

    return names.toString()
}

/**
 * An abstract representation of a templated action.
 */
interface ActionTemplate {
    val actionType: ActionType
    val actions: List<ActionConfig>
    val tokens: java.util.Queue<String>
    val placeholderValues: Map<String, String>

    fun accept(input: String): Boolean
    /**
     * The response message sent when this actionType is fired.
     */
    fun buildMessage(action: ActionConfig): String {
        return "I'm working on *${action.name}*..."
    }
}

/**
 * Parses tokens into placeholder values.
 */
abstract class AbstractActionTemplate : ActionTemplate {
    override val placeholderValues = java.util.HashMap<String, String>()

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
}

/**
 * Represents a simple operation for an action.
 */
abstract class GenericActionTemplate : AbstractActionTemplate() {
    protected val actionPlaceholder = "action name"

    override var actions: MutableList<ActionConfig> = mutableListOf()

    override fun accept(input: String): Boolean {
        val accepted = super.accept(input)

        // has action been set?
        if (accepted && tokens.isEmpty()) {
            val actionName = placeholderValues[actionPlaceholder]
            val actionConfigs = ConfigService.loadActions()

            val actionConfig = actionConfigs[actionName]
            if (null != actionConfig) {
                // exact action name match
                actions.add(actionConfig)

            } else {
                // check tags
                actionConfigs.values.forEach { action ->
                    action.tags?.filter { tag -> tag == actionName || actionName == "all" }?.forEach { tag -> actions.add(action) }
                }

                return (actions.size > 0)
            }
        }

        return accepted
    }
}

/**
 * Template to trigger job execution.
 */
class TriggerJobTemplate : AbstractActionTemplate {
    override val actionType: ActionType = ActionType.TRIGGER
    override var actions: List<ActionConfig>
    override val tokens: java.util.Queue<String>

    constructor(action: ActionConfig) {
        this.actions = mutableListOf(action)
        tokens = java.util.LinkedList(action.template.split("\\s".toRegex()).filterNot(String::isBlank))
    }

    override fun buildMessage(action: ActionConfig): String {
        val msg = StringBuilder()
        msg.append("I'm running *${action.name}*")

        if (placeholderValues.size > 0) {
            msg.append(" with these options:")
            placeholderValues.forEach { arg -> msg.append("\r\n- ${arg.key}: _${arg.value}_") }
        } else {
            msg.append(".")
        }

        return msg.toString()
    }
}

class EnableJobTemplate : GenericActionTemplate() {
    override val actionType: ActionType = ActionType.ENABLE
    override val tokens: java.util.Queue<String> = java.util.LinkedList(listOf("enable", "{${actionPlaceholder}}"))
}

class DisableJobTemplate : GenericActionTemplate() {
    override val actionType: ActionType = ActionType.DISABLE
    override val tokens: java.util.Queue<String> = java.util.LinkedList(listOf("disable", "{${actionPlaceholder}}"))
}

class LockActionTemplate : GenericActionTemplate() {
    override val actionType: ActionType = ActionType.LOCK
    override val tokens: java.util.Queue<String> = java.util.LinkedList(listOf("lock", "{${actionPlaceholder}}"))
}

class UnlockActionTemplate : GenericActionTemplate() {
    override val actionType: ActionType = ActionType.UNLOCK
    override val tokens: java.util.Queue<String> = java.util.LinkedList(listOf("unlock", "{${actionPlaceholder}}"))
}

class StatusJobTemplate : GenericActionTemplate() {
    override val actionType: ActionType = ActionType.STATUS
    override val tokens: java.util.Queue<String> = java.util.LinkedList(listOf("status", "{${actionPlaceholder}}"))
}
