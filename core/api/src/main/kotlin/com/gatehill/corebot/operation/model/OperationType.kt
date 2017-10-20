package com.gatehill.corebot.operation.model

/**
 * Operations that can be performed. Doubles as a list of named permissions in the security configuration.
 */
open class OperationType(val name: String, val description: String) {
    override fun toString(): String {
        return "OperationType(name='$name', description='$description')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        return (name == (other as OperationType).name)
    }

    override fun hashCode(): Int = name.hashCode()
}
