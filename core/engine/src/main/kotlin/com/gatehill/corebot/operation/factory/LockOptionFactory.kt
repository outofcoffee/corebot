package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.action.LockService
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.CoreOperationType
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.ConfigService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Locks an option value.
 */
@Template("lockOption", builtIn = true, showInUsage = true, operationMessageMode = OperationMessageMode.GROUP)
class LockOptionFactory @Inject constructor(configService: ConfigService,
                                            lockService: LockService,
                                            private val chatGenerator: ChatGenerator) : BaseLockableOptionFactory(configService, lockService) {

    override val operationType: OperationType = CoreOperationType.LOCK_OPTION

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) =
            "${chatGenerator.pleaseWait()}, I'm locking $optionName *$optionValue*..."

    override fun beforePerform(trigger: TriggerContext) {
        lockService.lockOption(optionName, optionValue, trigger.userId)
    }

    override fun buildCompleteMessage() = "I've locked :lock: $optionName *$optionValue* for you."
}
