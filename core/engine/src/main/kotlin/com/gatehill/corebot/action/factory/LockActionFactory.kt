package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.action.model.CoreOperationType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Locks an action.
 */
@Template("lockAction", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class LockActionFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val operationType: OperationType = CoreOperationType.LOCK_ACTION
}
