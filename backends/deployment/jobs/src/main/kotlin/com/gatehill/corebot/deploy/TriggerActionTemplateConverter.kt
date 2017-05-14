package com.gatehill.corebot.deploy

import com.gatehill.corebot.chat.ActionTemplateConverter
import com.gatehill.corebot.chat.model.template.ActionTemplate
import com.gatehill.corebot.chat.model.template.TriggerJobTemplate
import com.gatehill.corebot.config.model.ActionConfig

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TriggerActionTemplateConverter : ActionTemplateConverter {
    override fun convertConfigToTemplate(configs: Iterable<ActionConfig>): Collection<ActionTemplate> =
            configs.map { TriggerJobTemplate(it) }.toList()
}
