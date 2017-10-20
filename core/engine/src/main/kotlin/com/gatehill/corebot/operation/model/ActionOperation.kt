package com.gatehill.corebot.operation.model

import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.operation.factory.OperationFactory

/**
 * Represents an action operation to perform.
 */
class ActionOperation(override val operationType: OperationType,
                      override val operationFactory: OperationFactory,
                      override val shortDescription: String,
                      override val startMessage: String?,
                      override val tags: List<String>,
                      val driver: String,
                      val actionConfig: ActionConfig,
                      val args: Map<String, String>) : Operation(operationType, operationFactory, shortDescription, startMessage, tags) {

    override fun toString(): String {
        return "ActionOperation(operationType=$operationType, actionConfig=$actionConfig, args=$args)"
    }
}
