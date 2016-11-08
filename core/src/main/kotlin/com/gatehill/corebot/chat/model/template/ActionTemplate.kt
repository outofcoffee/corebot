package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.model.action.Action
import java.util.*

/**
 * An abstract representation of a templated action.
 */
interface ActionTemplate {
    val builtIn: Boolean
    val showInUsage: Boolean
    val actionType: ActionType
    val tokens: Queue<String>

    /**
     * Convert the action templates to a human-readable String.
     */
    val actionTemplates: String

    /**
     * Process the token and return true if it was accepted.
     */
    fun accept(input: String): Boolean

    /**
     * List the actions from this template.
     */
    fun buildActions(): List<Action>
}
