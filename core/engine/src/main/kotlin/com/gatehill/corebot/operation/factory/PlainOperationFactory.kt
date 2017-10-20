package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.operation.model.Operation
import com.gatehill.corebot.operation.model.PlainOperation
import com.gatehill.corebot.operation.model.TriggerContext

/**
 * Represents a plain operation.
 */
abstract class PlainOperationFactory : BaseOperationFactory() {
    override fun buildOperations(trigger: TriggerContext): List<Operation> {
        return listOf(PlainOperation(operationType,
                this,
                buildShortDescription(),
                if (operationMessageMode == OperationMessageMode.INDIVIDUAL) buildStartMessage(trigger) else null))
    }
}
