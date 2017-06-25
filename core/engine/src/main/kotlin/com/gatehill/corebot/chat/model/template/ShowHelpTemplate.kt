package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.TemplateService
import com.gatehill.corebot.chat.model.action.CoreActionType
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Shows a help/usage message.
 */
class ShowHelpTemplate @Inject constructor(private val templateService: TemplateService,
                                           private val chatGenerator: ChatGenerator) : SystemActionTemplate() {

    override val showInUsage = false
    override val actionType = CoreActionType.HELP
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${chatGenerator.greeting()} :simple_smile: Try one of these:\r\n${templateService.usage()}"
    }
}
