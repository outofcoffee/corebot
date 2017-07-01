package com.gatehill.corebot.driver.jobs.action

import com.gatehill.corebot.action.ActionFactoryConverter
import com.gatehill.corebot.action.factory.ActionFactory
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.jobs.action.factory.TriggerJobFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TriggerActionFactoryConverter @Inject constructor(private val chatGenerator: ChatGenerator) : ActionFactoryConverter {
    override fun convertConfigToFactory(configs: Iterable<ActionConfig>): Collection<ActionFactory> =
            configs.map { TriggerJobFactory(it, chatGenerator) }.toList()
}
