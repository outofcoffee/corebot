package com.gatehill.corebot.operation.model

/**
 * Groups operations and tracks successful executions.
 */
data class OperationContext(val operations: List<Operation>,
                            val groupStartMessage: String?,
                            val groupCompleteMessage: String?) {

    var successful: Int = 0
}
