package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.Operation
import com.gatehill.corebot.action.model.PlainOperation
import com.gatehill.corebot.action.model.TriggerContext

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
