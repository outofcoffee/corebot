package com.gatehill.corebot.backend.jobs.action.factory

import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.NamedActionFactory
import com.gatehill.corebot.operation.factory.Template
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Enables a job.
 */
@Template("enableJob", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class EnableJobFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val operationType: OperationType = JobOperationType.ENABLE
}
