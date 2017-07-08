package com.gatehill.corebot.driver.model

/**
 * Models the status of a triggered action.
 */
enum class ActionStatus {
    RUNNING,
    SUCCEEDED,
    FAILED,
    UNKNOWN;

    fun toSentenceCase(): String {
        return this.toString().substring(0..0).toUpperCase() + this.toString().substring(1).toLowerCase()
    }
}
