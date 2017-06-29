package com.gatehill.corebot.deploy

import com.gatehill.corebot.chat.template.ActionTemplateConverter
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.model.template.ActionTemplate
import com.gatehill.corebot.chat.model.template.TriggerJobTemplate
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TriggerActionTemplateConverter @Inject constructor(private val chatGenerator: ChatGenerator) : ActionTemplateConverter {
    override fun convertConfigToTemplate(configs: Iterable<ActionConfig>): Collection<ActionTemplate> =
            configs.map { TriggerJobTemplate(it, chatGenerator) }.toList()
}
