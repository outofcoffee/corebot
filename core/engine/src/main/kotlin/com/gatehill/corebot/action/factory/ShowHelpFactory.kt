package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.CoreActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.template.TemplateService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Shows a help/usage message.
 */
@Template("help")
class ShowHelpFactory @Inject constructor(private val templateService: TemplateService,
                                          private val chatGenerator: ChatGenerator) : SystemActionFactory() {

    override val showInUsage = false
    override val actionType = CoreActionType.HELP
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${chatGenerator.greeting()} :simple_smile: Try one of these:\r\n${templateService.usage()}"
    }
}
