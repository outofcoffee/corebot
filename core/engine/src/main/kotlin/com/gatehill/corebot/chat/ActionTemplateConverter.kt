package com.gatehill.corebot.chat

import com.gatehill.corebot.chat.model.template.ActionTemplate
import com.gatehill.corebot.config.model.ActionConfig

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface ActionTemplateConverter {
    fun convertConfigToTemplate(configs: Iterable<ActionConfig>): Collection<ActionTemplate>
}

class NoOpActionTemplateConverter : ActionTemplateConverter {
    override fun convertConfigToTemplate(configs: Iterable<ActionConfig>): Collection<ActionTemplate> = emptyList()
}
