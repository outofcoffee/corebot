package com.gatehill.corebot.chat.model.action

/**
 * Actions that can be performed. Doubles as a list of named permissions in the security configuration.
 */
open class ActionType(val name: String, val description: String) {
    override fun toString(): String {
        return "ActionType(name='$name', description='$description')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        return (name == (other as ActionType).name)
    }

    override fun hashCode(): Int = name.hashCode()
}
