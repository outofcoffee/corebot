package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.operation.model.CoreOperationType
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Unlocks an option value.
 */
@Template("unlockOption", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.GROUP)
class UnlockOptionFactory @Inject constructor(configService: ConfigService,
                                              lockService: LockService,
                                              private val chatGenerator: ChatGenerator) : BaseLockableOptionFactory(configService, lockService) {

    override val operationType: OperationType = CoreOperationType.UNLOCK_OPTION

    override fun beforePerform(trigger: TriggerContext) {
        lockService.unlockOption(optionName, optionValue)
    }

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${chatGenerator.pleaseWait()}, I'm unlocking $optionName *$optionValue*..."
    }

    override fun buildCompleteMessage() = "I've unlocked :unlock: $optionName *$optionValue* for you."
}
