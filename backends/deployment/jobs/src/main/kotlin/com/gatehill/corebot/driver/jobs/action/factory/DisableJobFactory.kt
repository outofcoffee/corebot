package com.gatehill.corebot.driver.jobs.action.factory

import com.gatehill.corebot.action.factory.OperationMessageMode
import com.gatehill.corebot.action.factory.NamedActionFactory
import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.config.ConfigService
import javax.inject.Inject

/**
 * Disables a job.
 */
@Template("disableJob", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class DisableJobFactory @Inject constructor(configService: ConfigService) : NamedActionFactory(configService) {
    override val operationType: OperationType = JobOperationType.DISABLE
}
