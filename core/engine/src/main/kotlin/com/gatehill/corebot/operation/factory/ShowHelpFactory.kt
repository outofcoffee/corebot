package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.operation.model.CoreOperationType
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.template.FactoryService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Shows a help/usage message.
 */
@Template("help", builtIn = true, showInUsage = false, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class ShowHelpFactory @Inject constructor(private val factoryService: FactoryService,
                                          private val chatGenerator: ChatGenerator) : PlainOperationFactory() {

    override val operationType = CoreOperationType.HELP

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${chatGenerator.greeting()} :simple_smile: Try one of these:\r\n${factoryService.usage()}"
    }
}
