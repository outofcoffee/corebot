package com.gatehill.corebot.operation.model

import com.gatehill.corebot.operation.factory.OperationFactory

/**
 * Represents a plain operation to perform.
 *
 * Every plain operation has the 'all' tag.
 */
class PlainOperation(override val operationType: OperationType,
                     override val operationFactory: OperationFactory,
                     override val shortDescription: String,
                     override val startMessage: String?) : Operation(operationType, operationFactory, shortDescription, startMessage, listOf("all"))
