package com.gatehill.corebot.operation.model

import com.gatehill.corebot.operation.factory.OperationFactory

/**
 * Represents an operation to perform.
 */
abstract class Operation(open val operationType: OperationType,
                         open val operationFactory: OperationFactory,
                         open val shortDescription: String,
                         open val startMessage: String?,
                         open val tags: List<String>) {

    override fun toString(): String {
        return "Operation(operationType=$operationType)"
    }
}
