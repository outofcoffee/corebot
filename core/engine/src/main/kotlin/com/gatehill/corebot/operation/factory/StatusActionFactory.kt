package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.CoreOperationType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Prints status information about an action.
 */
@Template("statusAction", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class StatusActionFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val operationType: OperationType = CoreOperationType.STATUS_ACTION
}
