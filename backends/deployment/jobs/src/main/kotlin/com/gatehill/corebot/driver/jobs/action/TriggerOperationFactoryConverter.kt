package com.gatehill.corebot.driver.jobs.action

import com.gatehill.corebot.action.OperationFactoryConverter
import com.gatehill.corebot.action.factory.OperationFactory
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.driver.jobs.action.factory.TriggerJobFactory
import javax.inject.Inject

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class TriggerOperationFactoryConverter @Inject constructor(private val chatGenerator: ChatGenerator) : OperationFactoryConverter {
    override fun convertConfigToFactory(configs: Iterable<ActionConfig>): Collection<OperationFactory> =
            configs.map { TriggerJobFactory(it, chatGenerator) }.toList()
}
