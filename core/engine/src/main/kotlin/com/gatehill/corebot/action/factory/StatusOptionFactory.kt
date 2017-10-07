package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.action.model.CoreOperationType
import com.gatehill.corebot.action.model.OperationType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Checks status of an option.
 */
@Template("statusOption", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.GROUP)
class StatusOptionFactory @Inject constructor(configService: ConfigService,
                                              lockService: LockService,
                                              private val chatGenerator: ChatGenerator) : BaseLockableOptionFactory(configService, lockService) {

    override val operationType: OperationType = CoreOperationType.STATUS_OPTION

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) =
            "${chatGenerator.pleaseWait()}, I'm checking the status of $optionName *$optionValue*..."

    override fun buildCompleteMessage(): String {
        val lock = lockService.checkOptionLock(optionName, optionValue)
        return lockService.describeLockStatus("Status of $optionName *$optionValue*: ", lock)
    }
}
