package com.gatehill.corebot.driver.jobs.action.factory

import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.action.model.CoreOperationType

/**
 * Extends the operation types with job specific operations.
 */
class JobOperationType(name: String, description: String) : CoreOperationType(name, description) {
    companion object {
        val TRIGGER = OperationType("TRIGGER", "trigger job")
        val ENABLE = OperationType("ENABLE", "enable job")
        val DISABLE = OperationType("DISABLE", "disable job")
    }
}
