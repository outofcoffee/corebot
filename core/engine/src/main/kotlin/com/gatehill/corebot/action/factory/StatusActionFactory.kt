package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.action.model.CoreOperationType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Prints status information about an action.
 */
@Template("statusAction", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class StatusActionFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val operationType: OperationType = CoreOperationType.STATUS_ACTION
}
