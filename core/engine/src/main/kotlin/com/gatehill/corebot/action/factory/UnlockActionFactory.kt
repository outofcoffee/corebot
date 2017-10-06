package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.action.model.CoreOperationType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Unlocks an action.
 */
@Template("unlockAction", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class UnlockActionFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val operationType: OperationType = CoreOperationType.UNLOCK_ACTION
}
